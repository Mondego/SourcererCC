def compareLisenseLists(licenses1, licenses2):
    if licenses1[0] == '' or licenses2[0] == '':
        return False
    for l1 in licenses1:
        for l2 in licenses2:
            if not ('deprecated' in l1) and not ('deprecated' in l2) and not compareLisenses(l1, l2):
                return False
    return True


def compareLisenses(license1, license2):
    license1 = renameLicense(license1)
    license2 = renameLicense(license2)
    if license1 == license2:
        return True
    return False
    # for l in licensesCompatibility[license1]:
    #   if str(license2) in l:
    #      return True
    # for l in licensesCompatibility[license2]:
    #   if str(license1) in l:
    #      return True
    # return False


def renameLicense(my_license):
    basicLicenses = ['MIT', 'Apache-2.0', 'GPL-2.0', 'GPL-3.0', 'LGPL-3.0', 'AGPL-3.0', 'BSD-3-Clause', 'BSD-2-Clause']
    for licenseType in basicLicenses:
        if licenseType in my_license:
            return licenseType
    return my_license


cloneGroups = [[]]


def addPairToCloneGroup(clone1, clone2):
    global cloneGroups
    if [] in cloneGroups:
        cloneGroups = [[clone1, clone2]]
        return
    else:
        for elem in cloneGroups:
            if clone1 in elem:
                if not (clone2 in elem):
                    elem.append(clone2)
                return
            if clone2 in elem:
                if not (clone1 in elem):
                    elem.append(clone1)
                return
        cloneGroups.append([clone1, clone2])


licenses = {}
licensesCompatibility = {
    'MIT': {'MIT', 'Apache-2.0', 'GPL-2.0', 'GPL-3.0', 'LGPL-3.0', 'AGPL-3.0', 'BSD-2-Clause', 'BSD-3-Clause'},
    'Apache-2.0': {'Apache-2.0', 'GPL-3.0', 'LGPL-3.0', 'AGPL-3.0'},
    'GPL-2.0': {'GPL-2.0'},
    'GPL-3.0': {'GPL-3.0', 'AGPL-3.0'},
    'LGPL-3.0': {'GPL-3.0', 'LGPL-3.0', 'AGPL-3.0'},
    'AGPL-3.0': {'AGPL-3.0'},
    'BSD-2-Clause': {'Apache-2.0', 'GPL-2.0', 'GPL-3.0', 'LGPL-3.0', 'AGPL-3.0', 'BSD-2-Clause', 'BSD-3-Clause'},
    'BSD-3-Clause': {'Apache-2.0', 'GPL-2.0', 'GPL-3.0', 'LGPL-3.0', 'AGPL-3.0', 'BSD-3-Clause'}
}

if __name__ == '__main__':
    with open("cloneGithub/nameToLicense.txt", 'r') as fin:
        for line in fin:
            c = line.split(';')
            licenses[c[0]] = []
            licenses[c[0]] += c[1].replace('\n', '').split(',')

    projectNameByNumber = {}
    i = 0
    with open('SourcererCC/tokenizers/block-level/project-list.txt', 'r') as fin:
        for line in fin:
            i = i + 1
            projectNameByNumber[i] = line[9:-5]

    with open("SourcererCC/results.pairs", 'r') as fin:
        for line in fin:
            x = line.split(',')
            if x[0] != x[2]:
                name1 = projectNameByNumber[int(x[0][1:])]
                name2 = projectNameByNumber[int(x[2][1:])]
                if not compareLisenseLists(licenses[name1], licenses[name2]):
                    addPairToCloneGroup(name1, name2)

    with open("filteredResults.txt", 'w') as fout:
        fout.write(len(cloneGroups))
        for elem in cloneGroups:
            fout.write(str(elem) + '\n\n')
