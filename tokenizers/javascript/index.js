const esprima = require('esprima')
const escodegen = require('escodegen')
const fs = require('fs-extra-promise')
const tokenizer = require('./tokenizer')

const immutable = require('immutable')
const walk = require('esprima-walk')

const { base64FileName } = require('./util')

const estools = require('estools')

const TOKENIZER_SCOPE_FILE = 'file-scope'
const TOKENIZER_SCOPE_FUNCTION = 'function-scope'

const TOKENIZER_SCOPE = TOKENIZER_SCOPE_FILE

// TODO: estools map / filter / traverse (instead of walk)
// - filter subfunctions from fuction asts somehow
// - test on SCC

// TODO: get rid of the function block and indentation
const regenerateFunctionCode = function(functionAst) {
  codegenOptions = { // NOTE: doesn't help
    format: {
      parentheses: false
    }
  }

  // NOTE: functionAst.body ommits the function signature (returns block only)
  return escodegen.generate(functionAst.body, {})
}

const processFile = function(fileName, data) {
  //let parentId = base64FileName(fileName) // TODO: incorporate repo name / hash
  let parentId = fileName
  let blockId = 1

  if (TOKENIZER_SCOPE === TOKENIZER_SCOPE_FILE) {
    return immutable.List.of(tokenizer(data, parentId, blockId))
  }

  options = {
    loc: true,
    range: true,
    comment: true,
    attachComment: true
  }
  fileAst = esprima.parse(data, {});

  let functions = immutable.List()
  let functionTokens = immutable.List()
  walk(fileAst, (node) => {
    if (node.type == 'FunctionExpression') {
      // const functionAstShallow = estools.map(node, (subNode) => {
      //   if (subNode === undefined || subNode.type === undefined) return
      //   if (subNode.type == 'FunctionExpression')
      //     return {}
      //   else return subNode
      // })
      //console.log(functionAstShallow)
      //process.exit(1)
      const functionAstShallow = node
      const functionCode = regenerateFunctionCode(functionAstShallow)
      functions = functions.push(functionCode)

      const tokenizedFunction = tokenizer(functionCode, parentId, blockId++)
      if (tokenizedFunction)
        functionTokens = functionTokens.push(tokenizedFunction)
    }
  })

  return functionTokens
}


const outputFile = function(functionTokens) {
  functionTokens.forEach((f) => {
    //console.log("===")
    console.log(f)
    //console.log("===")
  })
}

// TODO: check input
const fileName = process.argv[2]

fs.readFileAsync(fileName).then((data) => {
  outputFile(processFile(process.argv[3], data))
});
