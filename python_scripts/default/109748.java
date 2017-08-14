import java.io.*;
import java.util.Vector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.net.InetAddress;
import sun.misc.VMInspector;

public class cvmsh {

    static final int DEFAULT_PORT = 4321;

    protected int port = 0;

    protected Class serverSocketClass = null;

    protected Object serverSocket = null;

    protected CVMSHServer server = null;

    protected boolean isVerbose = false;

    protected boolean isServerMode = false;

    protected boolean hasQuitRequest = false;

    protected boolean shellIsRunning = false;

    protected boolean jvmpiSupported;

    protected Class cvmJvmpiClass;

    static final String[] helpMsg = { "Commands:", "  help                            - prints this list", "  quit                            - exits this shell nicely", "  quit!                           - exits this shell by calling OS exit()", "  detach                          - detaches the client (if applicable) but", "                                    leaves the VM and server running", "", "  sysinfo                         - dumps misc system information", "  gc                              - request a full GC cycle", "  memstat                         - prints VM heap memory usage", "  enableGC                        - enables the GC", "  disableGC                       - disables the GC", "  keepObjectsAlive true|false     - forces the GC to keep all objs alive (or not)", "", "  verbose                         - enables verbose output", "  quiet                           - disables verbose output", "  port=<port number>              - sets the server port number to listen on", "  startServer                     - starts server thread", "  stopServer                      - stops server thread", "", "  Dumpers:", "  ========", "  print <objAddr>                 - print object as string", "  dumpObject <objAddr>            - dump the specified object", "  dumpClassBlock <cbAddr>         - dump the specified class", "  dumpObjectReferences <objAddr>  - dump all references to the obj", "  dumpClassReferences <classname> - dump all references to instances of the class", "  dumpClassBlocks <classname>     - dump all CBs of specified name", "  dumpHeap [simple|verbose|stats] - dump heap contents or info", "      - does a simple(default), verbose, or stats dump.", "", "  Capturing and Comparing Heap states:", "  ====================================", "  captureHeapState [<comment>]    - capture the current heap state", "  releaseHeapState <id>           - release the specified heap state", "  releaseAllHeapStates            - release all heap states", "  listHeapStates                  - list captured heap states", "  dumpHeapState <id> [obj|class]  - dump the specified heap state", "      - sort by obj or obj's class or none if unspecified.", "  compareHeapState <id1> <id2>    - compares the objs in the heap states", "", "  Thread Queries:", "  ===============", "  listAllThreads                  - list all the currently live threads", "  dumpAllThreads                  - dump the stack trace of all the currently", "                                    live threads", "  dumpStack <eeAddr>              - dump the stack trace of the specified thread", "", "  Misc utilities:", "  ===============", "  time <command>                  - computes time to execute specified command", "  run <class> [args ...]          - runs the specified app synchronously", "  bg <class> [args ...]           - runs the specified app in a new thread" };

    static final String[] jvmpiHelpMsg = { "", "  JVMPI utilities:", "  ================", "  jvmpiDumpData                   - requests a JVMPI data dump" };

    public cvmsh() {
        try {
            serverSocketClass = Class.forName("java.net.ServerSocket");
        } catch (ClassNotFoundException e) {
        }
        try {
            cvmJvmpiClass = Class.forName("sun.misc.CVMJVMPI");
            jvmpiSupported = true;
        } catch (ClassNotFoundException e) {
            cvmJvmpiClass = null;
            jvmpiSupported = false;
        }
    }

    void doHelp() {
        printHelp(helpMsg);
        printJVMPIHelp();
    }

    void printHelp(String[] msg) {
        for (int i = 0; i < msg.length; i++) {
            System.out.println(msg[i]);
        }
    }

    protected void printJVMPIHelp() {
        if (jvmpiSupported) {
            printHelp(jvmpiHelpMsg);
        }
    }

    public boolean quitRequested() {
        return hasQuitRequest;
    }

    void doGC() {
        if (VMInspector.gcIsDisabled()) {
            System.err.println("ERROR: GC is currently disabled.  " + "Re-enable GC before invoking gc.");
            return;
        }
        java.lang.Runtime.getRuntime().gc();
        doMemstat();
    }

    void doMemstat() {
        Runtime r = java.lang.Runtime.getRuntime();
        System.out.println("free memory = " + r.freeMemory());
        System.out.println("total memory = " + r.totalMemory());
    }

    void keepObjectsAlive(CmdStream cmd) {
        String token = getToken(cmd);
        try {
            boolean keepAlive = getBoolean(token);
            VMInspector.keepAllObjectsAlive(keepAlive);
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: Expecting true or false instead of:" + token);
        }
    }

    Object getObject(String token) {
        Object obj = null;
        try {
            long addr = getLong(token);
            try {
                obj = VMInspector.addrToObject(addr);
            } catch (IllegalStateException e) {
                System.err.println("ERROR: Need to disable GC before " + "calling print");
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: address " + token + " is not a valid object");
            }
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid address format: " + token);
        }
        return obj;
    }

    long getAddr(String token) {
        long addr = 0L;
        try {
            addr = getLong(token);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid address format: " + token);
        }
        return addr;
    }

    void print(CmdStream cmd) {
        String token = getToken(cmd);
        Object obj = getObject(token);
        if (obj != null) {
            if (obj instanceof String) {
                String s = (String) obj;
                System.out.println("String " + token + ": length= " + s.length());
                System.out.println("   value= \"" + s + "\"");
            } else {
                System.out.println("Object " + token + ": " + obj);
            }
        }
    }

    void dumpObject(CmdStream cmd) {
        String token = getToken(cmd);
        long addr = getAddr(token);
        if (addr != 0L) {
            VMInspector.dumpObject(addr);
        }
    }

    void dumpClassBlock(CmdStream cmd) {
        String token = getToken(cmd);
        long addr = getAddr(token);
        if (addr != 0L) {
            VMInspector.dumpClassBlock(addr);
        }
    }

    void dumpObjectReferences(CmdStream cmd) {
        String token = getToken(cmd);
        long addr = getAddr(token);
        if (addr != 0L) {
            VMInspector.dumpObjectReferences(addr);
        }
    }

    void dumpClassReferences(CmdStream cmd) {
        String token = getToken(cmd);
        if (token != "") {
            VMInspector.dumpClassReferences(token);
        } else {
            System.err.println("ERROR: Classname not specified");
        }
    }

    void dumpClassBlocks(CmdStream cmd) {
        String token = getToken(cmd);
        if (token != "") {
            VMInspector.dumpClassBlocks(token);
        } else {
            System.err.println("ERROR: Classname not specified");
        }
    }

    void dumpHeap(CmdStream cmd) {
        String token = getToken(cmd);
        if (token.equals("")) {
            VMInspector.dumpHeapSimple();
        } else if (token.equals("simple")) {
            VMInspector.dumpHeapSimple();
        } else if (token.equals("verbose")) {
            VMInspector.dumpHeapVerbose();
        } else if (token.equals("stats")) {
            VMInspector.dumpHeapStats();
        } else {
            System.err.println("ERROR: Unsupported heap dump type: " + token);
        }
    }

    void captureHeapState(CmdStream cmd) {
        String name = cmd.input;
        VMInspector.captureHeapState(name);
    }

    void releaseHeapState(CmdStream cmd) {
        String token = getToken(cmd);
        try {
            int id = getInt(token);
            VMInspector.releaseHeapState(id);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid ID format: " + token);
        }
    }

    void dumpHeapState(CmdStream cmd) {
        String idToken = getToken(cmd);
        String sortKeyToken = getToken(cmd);
        try {
            int id = getInt(idToken);
            int sortKey = VMInspector.SORT_NONE;
            if (sortKeyToken.equals("obj")) {
                sortKey = VMInspector.SORT_BY_OBJ;
            } else if (sortKeyToken.equals("class")) {
                sortKey = VMInspector.SORT_BY_OBJCLASS;
            }
            VMInspector.dumpHeapState(id, sortKey);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid ID format: " + idToken);
        }
    }

    void compareHeapState(CmdStream cmd) {
        String token1 = getToken(cmd);
        String token2 = getToken(cmd);
        String currentToken = token1;
        try {
            int id1 = getInt(token1);
            currentToken = token2;
            int id2 = getInt(token2);
            VMInspector.compareHeapState(id1, id2);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid ID format: " + currentToken);
        }
    }

    void dumpStack(CmdStream cmd) {
        String token = getToken(cmd);
        try {
            int ee = getInt(token);
            VMInspector.dumpStack(ee);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid EE format: " + token);
        }
    }

    class BackgroundThread extends Thread {

        String className;

        String[] args;

        cvmsh shell;

        BackgroundThread(cvmsh shell, String className, String[] args) {
            this.className = className;
            this.args = args;
            this.shell = shell;
        }

        public void run() {
            shell.run(className, args, false);
        }
    }

    void run(String className, String[] args, boolean background) {
        if (!background) {
            try {
                Class cls = Class.forName(className);
                Class[] argClses = { String[].class };
                Method mainMethod = cls.getMethod("main", argClses);
                Object[] argObjs = { args };
                mainMethod.invoke(null, argObjs);
            } catch (InvocationTargetException ite) {
                ite.getTargetException().printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                BackgroundThread t = new BackgroundThread(this, className, args);
                t.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void jvmpiDumpData() {
        if (!jvmpiSupported) {
            return;
        }
        try {
            Method dataDumpMtd;
            Class[] argsTypes = new Class[0];
            dataDumpMtd = cvmJvmpiClass.getMethod("postDataDumpRequestEvent", argsTypes);
            Object[] voidArgs = new Object[0];
            dataDumpMtd.invoke(null, voidArgs);
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception while " + "requesting JVMPI data dump: " + e);
        }
    }

    class CmdStream {

        private static final char DELIMITER = ';';

        private static final String DELIMITER_STR = ";";

        CmdStream(String inputString) {
            input = inputString;
        }

        private String input;

        public boolean isDelimiter(String token) {
            if (token.equals(DELIMITER_STR) || token.equals("")) {
                return true;
            }
            return false;
        }

        public boolean isEmpty() {
            return ((input == null) || (input.equals("")));
        }

        public void clear() {
            input = null;
        }

        public String getToken() {
            String token;
            int wsIdx;
            int delimIdx;
            int index;
            int length;
            if (input == null) {
                return "";
            }
            length = input.length();
            for (wsIdx = 0; (wsIdx < length) && (input.charAt(wsIdx) == ' '); wsIdx++) ;
            length -= wsIdx;
            input = input.substring(wsIdx);
            wsIdx = input.indexOf(' ');
            delimIdx = input.indexOf(DELIMITER);
            if (delimIdx == 0) {
                input = input.substring(DELIMITER_STR.length());
                return DELIMITER_STR;
            }
            length = input.length();
            if (delimIdx == -1) delimIdx = length;
            if (wsIdx == -1) wsIdx = length;
            index = Math.min(delimIdx, wsIdx);
            token = input.substring(0, index);
            input = input.substring(index);
            return token;
        }

        public String[] getArgs() {
            String[] args = new String[0];
            Vector argsVec = new Vector();
            String token;
            token = getToken();
            while (!isDelimiter(token)) {
                argsVec.add(token);
                token = getToken();
            }
            args = (String[]) argsVec.toArray(args);
            return args;
        }
    }

    public String getToken(CmdStream cmd) {
        return cmd.getToken();
    }

    String[] getArgs(CmdStream cmd) {
        return cmd.getArgs();
    }

    static int getInt(String token) throws NumberFormatException {
        int result;
        int radix = 10;
        if (token.startsWith("0x")) {
            int index;
            index = token.indexOf('x');
            token = token.substring(index + 1);
            radix = 16;
        }
        result = Integer.parseInt(token, radix);
        return result;
    }

    static long getLong(String token) throws NumberFormatException {
        long result;
        int radix = 10;
        if (token.startsWith("0x")) {
            int index;
            index = token.indexOf('x');
            token = token.substring(index + 1);
            radix = 16;
        }
        result = Long.parseLong(token, radix);
        return result;
    }

    static boolean getBoolean(String token) throws IllegalArgumentException {
        boolean result = false;
        if (token.toLowerCase().equals("true")) {
            result = true;
        } else if (token.toLowerCase().equals("false")) {
            result = false;
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    boolean processCmd(CmdStream cmd, CVMSHRunnable server) {
        boolean done;
        do {
            String token = getToken(cmd);
            done = processToken(token, cmd, server);
            if (server != null) {
                done = done || server.isStopped();
            }
        } while (!done && !cmd.isEmpty());
        return done;
    }

    boolean processToken(String token, CmdStream cmd, CVMSHRunnable server) {
        if (isVerbose && !cmd.isDelimiter(token)) {
            System.out.println("command: " + token);
        }
        if (token.equals("quit")) {
            hasQuitRequest = true;
            if (isServerMode) {
                stopServer((server == null));
            }
            return true;
        } else if (token.equals("quit!")) {
            VMInspector.exit(0);
        } else if (token.equals("detach")) {
            return true;
        } else if (cmd.isDelimiter(token)) {
            return false;
        } else if (token.equals("help")) {
            doHelp();
        } else if (token.equals("sysinfo")) {
            VMInspector.dumpSysInfo();
        } else if (token.equals("gc")) {
            token = null;
            doGC();
        } else if (token.equals("memstat")) {
            doMemstat();
        } else if (token.equals("enableGC")) {
            VMInspector.enableGC();
        } else if (token.equals("disableGC")) {
            VMInspector.disableGC();
        } else if (token.equals("keepObjectsAlive")) {
            keepObjectsAlive(cmd);
        } else if (token.equals("verbose")) {
            if (!isVerbose) {
                System.out.println("mode: verbose ON");
            }
            isVerbose = true;
        } else if (token.equals("quiet")) {
            if (isVerbose) {
                System.out.println("mode: verbose OFF");
            }
            isVerbose = false;
        } else if (token.startsWith("port=")) {
            synchronized (this) {
                if (isServerMode) {
                    System.err.println("ERROR: Cannot change port number " + "while the server mode has been started.");
                    return false;
                }
                String portStr = token.substring(token.indexOf('=') + 1);
                try {
                    int value = getInt(portStr);
                    if (isVerbose) {
                        System.out.println("set port: " + value);
                    }
                    port = value;
                } catch (NumberFormatException e) {
                    System.err.println("ERROR: Invalid port number: " + portStr);
                }
            }
        } else if (token.equals("startServer")) {
            startServer();
        } else if (token.equals("stopServer")) {
            stopServer((server == null));
            if (server != null) {
                return true;
            }
        } else if (token.equals("print")) {
            print(cmd);
        } else if (token.equals("dumpObject")) {
            dumpObject(cmd);
        } else if (token.equals("dumpClassBlock")) {
            dumpClassBlock(cmd);
        } else if (token.equals("dumpObjectReferences")) {
            dumpObjectReferences(cmd);
        } else if (token.equals("dumpClassReferences")) {
            dumpClassReferences(cmd);
        } else if (token.equals("dumpClassBlocks")) {
            dumpClassBlocks(cmd);
        } else if (token.equals("dumpHeap")) {
            dumpHeap(cmd);
        } else if (token.equals("captureHeapState")) {
            captureHeapState(cmd);
        } else if (token.equals("releaseHeapState")) {
            releaseHeapState(cmd);
        } else if (token.equals("releaseAllHeapStates")) {
            VMInspector.releaseAllHeapState();
        } else if (token.equals("listHeapStates")) {
            VMInspector.listHeapStates();
        } else if (token.equals("dumpHeapState")) {
            dumpHeapState(cmd);
        } else if (token.equals("compareHeapState")) {
            compareHeapState(cmd);
        } else if (token.equals("listAllThreads")) {
            VMInspector.listAllThreads();
        } else if (token.equals("dumpAllThreads")) {
            VMInspector.dumpAllThreads();
        } else if (token.equals("dumpStack")) {
            dumpStack(cmd);
        } else if (token.equals("run")) {
            String classname = getToken(cmd);
            String[] args = getArgs(cmd);
            run(classname, args, false);
        } else if (token.equals("bg")) {
            String classname = getToken(cmd);
            String[] args = getArgs(cmd);
            run(classname, args, true);
        } else if (token.equals("time")) {
            long startTime = System.currentTimeMillis();
            processCmd(cmd, server);
            long endTime = System.currentTimeMillis();
            System.out.println("Time elapsed: " + (endTime - startTime) + " ms");
        } else if (jvmpiSupported && token.equals("jvmpiDumpData")) {
            jvmpiDumpData();
        } else {
            System.err.print("Unknown command: \"" + token);
            if (!cmd.isEmpty()) {
                System.err.print(cmd.input);
                cmd.clear();
            }
            System.err.println("\"");
            System.err.println("type \"help\" for available commands");
        }
        return false;
    }

    public void runShell() {
        shellIsRunning = true;
        serviceInput(System.in, System.out, null);
    }

    public void serviceInput(InputStream inStream, OutputStream outStream, CVMSHRunnable server) {
        InputStreamReader in = new InputStreamReader(inStream);
        int numberOfChars;
        char[] buf = new char[1000];
        boolean promptDisturbed = false;
        if (isVerbose && (server != null)) {
            System.err.println("STATUS: client has attached");
            promptDisturbed = true;
        }
        try {
            boolean done = false;
            final char CR = (char) 0xD;
            final char LF = (char) 0xA;
            while (!done && !quitRequested()) {
                String input;
                if ((server == null) || (shellIsRunning && promptDisturbed)) {
                    System.out.print("cvmsh> ");
                }
                numberOfChars = in.read(buf, 0, buf.length);
                if ((numberOfChars > 0) && (buf[numberOfChars - 1] == LF)) {
                    numberOfChars--;
                }
                if ((numberOfChars > 0) && (buf[numberOfChars - 1] == CR)) {
                    numberOfChars--;
                }
                input = new String(buf, 0, numberOfChars);
                if (server != null) {
                    if (!shellIsRunning) {
                        System.out.print("cvmsh> ");
                    }
                    System.out.println(input);
                }
                CmdStream cmd = new CmdStream(input);
                input = null;
                done = processCmd(cmd, server);
                if (server != null) {
                    promptDisturbed = true;
                    done = done || server.isStopped();
                }
            }
        } catch (SocketException e) {
            if (isVerbose && isServerMode) {
                System.err.println("STATUS: client has detached");
                promptDisturbed = true;
            }
        } catch (IOException e) {
            if (isVerbose && isServerMode) {
                System.err.println("STATUS: no client attached");
                promptDisturbed = true;
            }
        } catch (Throwable e) {
            if (isVerbose && isServerMode) {
                System.err.println("STATUS: lost connection with client");
                promptDisturbed = true;
            } else {
                System.err.println("ERROR: " + e);
                promptDisturbed = true;
            }
        }
        if (promptDisturbed && shellIsRunning) {
            System.out.print("cvmsh> ");
        }
    }

    /**
     * Starts a socket server to service incoming requests to cvmsh.
     */
    public synchronized void startServer() {
        if (serverSocketClass == null) {
            System.err.println("Server mode not supported: need Foundation Profile or higher");
            return;
        }
        if (isServerMode) {
            if (isVerbose) {
                System.err.println("Server already started on port " + port);
            }
            return;
        }
        isServerMode = true;
        if (port == 0) {
            port = DEFAULT_PORT;
            if (isVerbose) {
                System.out.println("port not set.  Using default port: " + port);
            }
        }
        try {
            Class[] argsTypes = new Class[1];
            argsTypes[0] = Integer.TYPE;
            Constructor ct = serverSocketClass.getConstructor(argsTypes);
            Object[] argsList = new Object[1];
            argsList[0] = new Integer(port);
            serverSocket = ct.newInstance(argsList);
            server = new CVMSHServer(this, serverSocketClass, serverSocket, port);
            Thread serverThread = new Thread(server);
            serverThread.start();
        } catch (InvocationTargetException e) {
            System.err.println("ERROR: Cannot start server on port " + port);
            return;
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception while " + "creating server: " + e);
            return;
        }
    }

    /**
     * Stop the previously started socket server.
     */
    public synchronized void stopServer(boolean calledFromClient) {
        if (!isServerMode) {
            if (isVerbose) {
                System.err.println("Server not started yet.  No need to stop");
            }
            return;
        }
        server.stop(calledFromClient);
    }

    /**
     * Report that the server is stopping.
     */
    public synchronized void serverStopped() {
        if (isServerMode) {
            try {
                Class[] argsTypes = new Class[0];
                Method closeMtd;
                closeMtd = serverSocketClass.getMethod("close", argsTypes);
                Object[] voidArgs = new Object[0];
                closeMtd.invoke(serverSocket, voidArgs);
            } catch (InvocationTargetException ioe) {
                System.err.println("ERROR: IO error while closing " + "server socket");
            } catch (Exception e) {
                System.err.println("ERROR: Unexpected exception while " + "closing server socket: " + e);
                return;
            }
            serverSocket = null;
            server = null;
            isServerMode = false;
        }
    }

    /**
     * Starts a socket server to service incoming requests to cvmsh.
     * @returns true if we're done with cvmsh.  Else, returns false.
     */
    public boolean parseOptions(String[] args) {
        boolean done = false;
        boolean foundMainClass = false;
        for (int i = 0; !done && i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--X")) {
                if (++i < args.length) {
                    CmdStream cmd = new CmdStream(args[i]);
                    done = processCmd(cmd, null);
                }
            } else {
                String className = arg;
                int numberOfArgs = args.length - i - 1;
                String[] newArgs = new String[numberOfArgs];
                for (int j = 0; j < numberOfArgs; j++) {
                    newArgs[j] = args[i + j + 1];
                }
                i += numberOfArgs;
                foundMainClass = true;
                run(className, newArgs, false);
            }
        }
        if (!done && !foundMainClass && !isServerMode) {
            runShell();
        }
        return done;
    }

    public static void main(String[] args) {
        cvmsh sh = new cvmsh();
        sh.parseOptions(args);
    }
}

interface CVMSHRunnable {

    boolean isStopped();
}

class CVMSHServer implements Runnable, CVMSHRunnable {

    boolean stopRequested = false;

    boolean isStopped = false;

    cvmsh shell;

    int port;

    Object[] voidArgs;

    Class serverSocketClass = null;

    Method acceptMtd;

    Object serverSocket;

    Class socketClass;

    Method closeMtd;

    Method getOutputStreamMtd;

    Method getInputStreamMtd;

    protected Object client;

    protected OutputStream outStream;

    protected InputStream inStream;

    CVMSHServer(cvmsh shell, Class socketClass, Object socket, int port) throws Exception {
        this.shell = shell;
        this.serverSocketClass = socketClass;
        this.serverSocket = socket;
        this.port = port;
        Class[] voidArgsTypes = new Class[0];
        voidArgs = new Object[0];
        acceptMtd = serverSocketClass.getMethod("accept", voidArgsTypes);
        socketClass = Class.forName("java.net.Socket");
        closeMtd = socketClass.getMethod("close", voidArgsTypes);
        getOutputStreamMtd = socketClass.getMethod("getOutputStream", voidArgsTypes);
        getInputStreamMtd = socketClass.getMethod("getInputStream", voidArgsTypes);
    }

    synchronized void shutDown() {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                System.err.println("ERROR closing server output stream: " + e);
            }
        }
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException e) {
                System.err.println("ERROR closing server input stream: " + e);
            }
        }
        if (client != null) {
            try {
                closeMtd.invoke(client, voidArgs);
            } catch (InvocationTargetException e) {
                System.err.println("ERROR closing socket: " + e.getTargetException());
            } catch (Exception e) {
                System.err.println("ERROR: Unexpected exception while " + "closing socket: " + e);
            }
            client = null;
        }
        isStopped = true;
        notifyAll();
    }

    public void run() {
        while (!shell.quitRequested() && !stopRequested) {
            try {
                client = acceptMtd.invoke(serverSocket, voidArgs);
            } catch (InvocationTargetException e) {
                System.err.println("ERROR: Failed to accept on port: " + port);
                shutDown();
                return;
            } catch (Exception e) {
                System.err.println("ERROR: Unexpected exception while " + "accepting server connection: " + e);
                return;
            }
            try {
                outStream = (OutputStream) getOutputStreamMtd.invoke(client, voidArgs);
                inStream = (InputStream) getInputStreamMtd.invoke(client, voidArgs);
            } catch (InvocationTargetException e) {
                System.err.println("ERROR: Failed to get client IO streams");
                shutDown();
                return;
            } catch (Exception e) {
                System.err.println("ERROR: Unexpected exception while " + "getting client IO streams: " + e);
                return;
            }
            shell.serviceInput(inStream, outStream, this);
        }
        shutDown();
    }

    public synchronized void stop(boolean calledFromClient) {
        stopRequested = true;
        if (calledFromClient) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        shell.serverStopped();
    }

    public boolean isStopped() {
        return isStopped;
    }

    ;
}
