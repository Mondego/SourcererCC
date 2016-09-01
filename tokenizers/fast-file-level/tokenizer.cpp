//
// Thx to Peta Maj https://git.io/vitYj
//
//

#include <ctime>
#include <cstdlib>
#include <iostream>
#include <map>
#include <unordered_set>
#include <ciso646>
#include <cassert>
#include <sstream>
#include <fstream>
#include <vector>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include <cstring>

std::string const PATH_proj_paths = "../projects.txt";
std::string const PATH_tokens_folder = "tokens";
std::string const PATH_bookkeeping_file_folder = "bookkeeping_files";
std::string const PATH_bookkeeping_proj_folder = "bookkeeping_projs";
std::string const PATH_projects_success = "projects_success.txt";
std::string const PATH_projects_starting_index = "project_startingt_index.txt";
std::string const PATH_projects_fail = "prohjects_fail.txt";


/** Little class that matches separators.

  A simple matching FSM. Can be made faster if needs be, using tables.
 */
class SeparatorMatcher {
public:

    SeparatorMatcher():
        current_(initial_) {
    }

    /** Advances the current state using given character.

      Returns true if an advancement was possible, false if the matcher had to reset.
     */
    bool match(char c) {
        auto i = current_->next.find(c);
        if (i == current_->next.end()) {
            current_ = initial_;
            i = current_->next.find(c);
            if (i != current_->next.end())
                current_ = i->second;
            return false;
        } else {
            current_ = i->second;
            return true;
        }
    }

    /** Returns the length of matched separator.

      0 if no separator has been found.
     */
    unsigned matched() const {
        return current_->result;
    }

    static void addMatch(std::string const & what) {
        initial_->addMatch(what, 0);
    }

    static void initializeJS() {
        addMatch(" ");
        addMatch("\n");
        addMatch("\t");
        addMatch(";");
        addMatch("::");
        addMatch(".");
        addMatch(",");
        addMatch("->");
        addMatch("[");
        addMatch("]");
        addMatch("(");
        addMatch(")");
        addMatch("++");
        addMatch("--");
        addMatch("~");
        addMatch("!");
        addMatch("-");
        addMatch("+");
        addMatch("&");
        addMatch("*");
        addMatch(".*");
        addMatch("->*");
        addMatch("/");
        addMatch("%");
        addMatch("<<");
        addMatch(">>");
        addMatch("<");
        addMatch(">");
        addMatch("<=");
        addMatch(">=");
        addMatch("!=");
        addMatch("^");
        addMatch("|");
        addMatch("&&");
        addMatch("||");
        addMatch("?");
        addMatch("==");
        addMatch("{");
        addMatch("}");
        addMatch("=");
        addMatch("#");
        addMatch("\"");
        addMatch("\\");
        addMatch(":");
        addMatch("$");
        addMatch("'");
    }


private:

    class State {
    public:
        unsigned result = 0;
        std::map<char, State *> next;

        void addMatch(std::string const & what, unsigned index) {
            if (index == what.size()) {
                assert(result == 0 and "Ambiguous match should not happen");
                result = index;
            } else {
                auto i = next.find(what[index]);
                if (i == next.end())
                    i = next.insert(std::pair<char, State*>(what[index], new State())).first;
                i->second->addMatch(what, index +1);
            }
        }
    };

    static State * initial_;
    State * current_;

};

SeparatorMatcher::State * SeparatorMatcher::initial_ = new SeparatorMatcher::State();

// output stream for tokenization results
std::ofstream tokens_file;
// file paths bookkeeping
std::ofstream bookkeeping_file;
// project directories bookkeeping
std::ofstream bookkeeping_proj;

// total files tokenized
unsigned total_files = 0;
// file id in current project
unsigned file_id = 0;
// project id
unsigned project_id = 0;
// last known duration in [s] (for once per second stats)
unsigned lastDuration = 0;
// total number of parsed bytes
unsigned long long bytes = 0;

// start of the tokenization for timing purposes
std::clock_t start;


/** Tokenizes a JS file in single pass.
 */
class Tokenizer {
public:

    /** Tokenizes the given file.

     */
    static void tokenize(std::istream & file, std::ostream & output) {
        Tokenizer t(file);
        t.tokenize();
        t.writeCounts(output);
    }

    /** Opens the given file and tokenizes it into the current output in the sourcerer format.
     */
    static void tokenize(std::string const & filename, std::ostream & output) {
        std::ifstream input(filename);
        if (not input.good()) {
            std::cerr << "Unable to open file " << filename << std::endl;
            return;
        }
        output << project_id << "," << file_id << "@#@";
        tokenize(input, output);
    }

private:

    enum class State {
        Ready,
        Comment1,
        LineComment,
        MultiComment,
        MultiCommentMayEnd,
    };

    Tokenizer(std::istream & file):
        f_(file) {
    }

    /** The main loop deals with comments, ignoring tokens inside them and moving between normal state, line comments and multi-line comments.
     */
    void tokenize() {
        State state = State::Ready;
        while (true) {
            char c = f_.get();
            if (f_.eof())
                break;
            ++bytes;
            switch (state) {
                case State::Ready:
                    if (c == '/')
                        state = State::Comment1;
                    checkToken(c);
                    break;
                case State::Comment1:
                    if (c == '/')
                        state = State::LineComment;
                    else if (c == '*')
                        state = State::MultiComment;
                    else
                        state = State::Ready;
                    checkToken(c);
                    break;
                case State::LineComment:
                    if (c == '\n') {
                        state = State::Ready;
                        reset();
                    }
                    break;
                case State::MultiComment:
                    if (c == '*')
                        state = State::MultiCommentMayEnd;
                    break;
                case State::MultiCommentMayEnd:
                    if (c == '/') {
                        state = State::Ready;
                        reset();
                    } else {
                        state = State::MultiComment;
                    }
                    break;
            }
        }
        // TODO we must add token if there is last one too
        if (not current_.empty() and not inSeparator_)
            ++tokens_[current_];
    }

    /** With given new character, checks whether separator has been matched, in which case the token parsed so far should be accounted. Then flips into separator matching phase (so that we correctly match the longest available separator (i.e. a== will trigger after first =, but must parse the second one correctly as well.
     */
    void checkToken(char c) {
        current_ += c;
        if (not matcher_.match(c) and inSeparator_)
            inSeparator_ = false;
        unsigned matchedLength = matcher_.matched();
        if (matchedLength > 0) {
            if (not inSeparator_) {
                std::string token = current_.substr(0, current_.length() - matchedLength);
                if (not token.empty()) {
                    // starts at 0
                    ++tokens_[token];
                }
                inSeparator_ = true;
            }
            current_.clear();
        }
    }

    /** Resets the tokenizer so that it can accept new tokens.
     */
    void reset() {
        current_.clear();
        inSeparator_ = false;
    }

    /** Writes the token counts into the output stream using the sourcerer's notation.
     */
    void writeCounts(std::ostream & output) const {
        for (auto i: tokens_)
            output << "," << i.first << "@@::@@" << i.second;
    }

    // counted tokens
    std::map<std::string, unsigned> tokens_;

    // input
    std::istream & f_;

    // currently parsed token
    std::string current_;

    // continued separator matching after token addition
    bool inSeparator_ = false;

    // matcher used for separator detection
    SeparatorMatcher matcher_;

};


/** Prints statistics about bitrate and processed files.
 */
void printStats() {
    std::clock_t now = std::clock();
    double ms = (now - start) / (double)(CLOCKS_PER_SEC / 1000);
    unsigned d = ms / 1000;
    if (d != lastDuration) {
        std::cout << "Processed files " << total_files << ", throughput " << (bytes / (ms / 1000) / 1024 / 1024) << "[MB/s]" << std::endl;
        lastDuration = d;
    }
}

/** Simple helper that checks whether string ends with given characters.
 */
bool endsWith(std::string const & str, std::string const & suffix) {
   if (str.length() >= suffix.length()) {
       return (0 == str.compare(str.length() - suffix.length(), suffix.length(), suffix));
   } else {
       return false;
   }
}

/** Tokenizes all javascript files in the directory. Recursively searches in subdirectories.
 */
void tokenizeDirectory(DIR * dir, std::string path) {
    struct dirent * ent;
    while ((ent = readdir(dir)) != nullptr) {
        if (strcmp(ent->d_name, ".") == 0 or strcmp(ent->d_name, "..") == 0)
            continue;
        std::string p = path + "/" + ent->d_name;
        DIR * d = opendir(p.c_str());
        if (d != nullptr) {
            tokenizeDirectory(d, p);
        } else if (endsWith(ent->d_name, ".js")) {
            std::string filename = path + "/" + ent->d_name;

            Tokenizer::tokenize(filename, tokens_file);
            tokens_file << std::endl;

            printStats();

            bookkeeping_file << project_id << "," << file_id << "," << filename << std::endl;
            ++file_id;
            ++total_files;
        }
    }
    closedir(dir);
}

/** Resets the file id, then walks the project directory recursively and analyzes all its JS files.

  Finally updates the project bookkeeping file with project id and location.
 */
void tokenizeProject(DIR * dir, std::string path) {
    file_id = 0;
    tokenizeDirectory(dir, path);
    bookkeeping_proj << project_id << "," << path << std::endl;
    ++project_id;
}

/** Initializes output directories if not yet created.
 */
void initializeDirectories() {
    /** Initialize directories if they do not exist */
    mkdir(PATH_tokens_folder.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
    mkdir(PATH_bookkeeping_file_folder.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
    mkdir(PATH_bookkeeping_proj_folder.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
}

/** Opens the tokens and bookkeeping streams with unique id attached.
 */
void openUniqueStreams() {
    unsigned result = 0;
    while (true) {
        ++result;
        std::string num = std::to_string(result);
        std::ifstream f(PATH_tokens_folder + "/tokens_" + num + ".txt");
        if (f.good())
            continue;
        f.open(PATH_bookkeeping_file_folder + "/bookkeeping_file_" + num + ".txt");
        if (f.good())
            continue;
        f.open(PATH_bookkeeping_file_folder + "/bookkeeping_proj_" + num + ".txt");
        if (f.good())
            continue;
        break;
    }
    std::string num = std::to_string(result);
    std::cout << "This run ID is: " << num << std::endl;
    tokens_file.open(PATH_tokens_folder + "/tokens_" + num + ".txt");
    bookkeeping_file.open(PATH_bookkeeping_file_folder + "/bookkeeping_file_" + num + ".txt");
    bookkeeping_proj.open(PATH_bookkeeping_proj_folder + "/bookkeeping_proj_" + num + ".txt");
}

int main(int argc, char *argv[]) {

    if (argc != 2) {
        std::cerr << "Invalid usage! You must specify root folder where to look for the projects";
        return EXIT_FAILURE;
    }

    // initialize
    SeparatorMatcher::initializeJS();
    initializeDirectories();
    openUniqueStreams();

    // start timing
    start = std::clock();

    // walk the root directory and find projects
    std::string rootPath = argv[1];
    DIR * root = opendir(rootPath.c_str());
    if (root == nullptr) {
        std::cerr << "Unable to open root directory " << rootPath << std::endl;
        return EXIT_FAILURE;
    }
    struct dirent * ent;
    while ((ent = readdir(root)) != nullptr) {
        if (strcmp(ent->d_name, ".") == 0 or strcmp(ent->d_name, "..") == 0)
            continue;
        std::string p = rootPath + "/" + ent->d_name;
        DIR * d = opendir(p.c_str());
        if (d != nullptr)
            tokenizeProject(d, p);
    }
    closedir(root);

    // print final stats
    std::clock_t now = std::clock();
    double ms = (now - start) / (double)(CLOCKS_PER_SEC / 1000);

    std::cout << "Total bytes: " << (bytes / (double) 1024 / 1024) << "[MB]" << std::endl;
    std::cout << "Total time:  " << (ms/1000) << " [s]" << std::endl;
    std::cout << "Processed " << total_files << " files in " << project_id << " projects, throughput " << (bytes / (ms / 1000) / 1024 / 1024) << "[MB/s]" << std::endl;

    return EXIT_SUCCESS;
}
