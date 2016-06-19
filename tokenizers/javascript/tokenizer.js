const _ = require('lodash')
const immutable = require('immutable')
const fs = require('fs-extra-promise')
const esprima = require('esprima')

const MAIN_DELIMITER = '@#@'
const COUNT_DELIMITER = '@@::@@'
const TOKEN_DELIMITER = ','
const TOKEN_DELIMITER_REPLACEMENT = "_"
const WHITESPACES = /(\s+)/g

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
// jakubkoo.. ako sa spraví to šedé s hviezdičkou, že dve a tri
// že kde presne interakcie sú v organizme
const tokenFilters = tokenTypes.map((tokenType) => {
  return _.partial(filterTokens, tokenType)
})

// NOTE: Filter out hashbang lines
const HASHBANG = /^#!/
const filterHashbangLine = function(code) {
  const firstLineLoc = code.indexOf('\n')
  const firstLine = code.slice(0, firstLineLoc).toString()
  if (firstLine.search(HASHBANG) == -1)
    return code

  return code.slice(firstLineLoc)
}

// TODO: handle "#!/usr/bin/env node"
// TODO: handle
const tokenizer = function(code, parentId, blockId) {
  const options = { }
  tokensRaw = esprima.tokenize(filterHashbangLine(code), options)

  // TODO: refactor these
  const tokens = immutable.List(tokensRaw).flatMap((token) => {
    if (token.value.indexOf(TOKEN_DELIMITER) != -1)
      tokenDelimiters = new RegExep(TOKEN_DELIMITER, 'g')
      token.value =
        token.value.replace(tokenDelimiters, TOKEN_DELIMITER_REPLACEMENT)

    // NOTE: get rid of all whitespaces, dey sak
    if (token.value.search(WHITESPACES) != -1)
      token.value = token.value.replace(WHITESPACES, '')

    // NOTE: skip RegExes, SCC has weird problems with it
    if (token.type == 'RegularExpression')
      return immutable.List()

    //if (token.type != 'String')
    return immutable.List.of(token);

    // NOTE: now it's string
    // const stringTokensRaw = token.value.split(WHITESPACE)
    // const stringTokens = stringTokensRaw.map((stringToken) => {
    //   return { value: stringToken }
    // })
    // return immutable.List(stringTokens)
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

  if (tokenPairs.size == 0)
    return ''

  const lhs = `${parentId},${blockId},`
  const rhs = tokenPairs.join(TOKEN_DELIMITER)
  const output = `${lhs}${MAIN_DELIMITER}${rhs}`

  return output
};

module.exports = tokenizer
