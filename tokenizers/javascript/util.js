
const base64FileName = function(fileName) {
  const fileNameBuffer = Buffer.from(fileName)
  return fileNameBuffer.toString('base64')
}


module.exports = {
  base64FileName
}
