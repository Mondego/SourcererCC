const _ = require('lodash')
const immutable = require('immutable')
const fs = require('fs-extra-promise')
const esprima = require('esprima')

const MAIN_DELIMITER = '@#@'
const COUNT_DELIMITER = '@@::@@'
const TOKEN_DELIMITER = ','
const TOKEN_DELIMITER_REPLACEMENT = "_"
const WHITESPACE = /(\s+)/

const filterTokens = function (type, token) {
  return token.type == type
}


// NOTE: http://esprima.org/doc/#usage
const tokenTypes = immutable.List.of(
  'Boolean',
  'Identifier',
  'Keyword',
  'Null',
  'Numeric', 
  'Punctuator',
  'String',
  'RegularExpression'
)

const tokenFilters = tokenTypes.map((tokenType) => {
  return _.partial(filterTokens, tokenType)
})

const tokenizer = function(code, parentId, blockId) {
  const options = { }
  // TODO: refactor this
  const tokens = immutable.List(esprima.tokenize(code, options)).flatMap((token) => {
    if (token.value.indexOf(TOKEN_DELIMITER) != -1)
      token.value =
        token.value.replace(TOKEN_DELIMITER, TOKEN_DELIMITER_REPLACEMENT)

    if (token.type != 'String')
      return immutable.List.of(token);

    // NOTE: now it's string
    const stringTokensRaw = token.value.split(WHITESPACE)
    const stringTokens = stringTokensRaw.map((stringToken) => {
      return { value: stringToken }
    })
    return immutable.List(stringTokens)
  })

  // TODO: reduce to map
  // const filteredTokens = tokenFilters.map((tokenFilter) => {
  //  return tokens.filter(tokenFilter)
  // })

  let uniqueTokens = immutable.Map()
  tokens.forEach((token) => {
    if (uniqueTokens.has(token.value)) {
      newUniqueTokens = uniqueTokens.updateIn(
        [ token.value ],
        (count) => {
          return count + 1
        })
    } else {
      newUniqueTokens = uniqueTokens.set(token.value, 1)
    }
    uniqueTokens = newUniqueTokens
  })

  const tokenPairs = uniqueTokens.map((count, token) => {
    return `${token}${COUNT_DELIMITER}${count}`
  })

  const lhs = `${parentId},${blockId},`
  const rhs = tokenPairs.join(TOKEN_DELIMITER)
  const output = `${lhs}${MAIN_DELIMITER}${rhs}`

  return output
};

module.exports = tokenizer
