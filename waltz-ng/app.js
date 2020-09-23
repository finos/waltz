const express = require('express');
const url = require('url');
const apiMocker = require('connect-api-mocker');
const cors = require('cors');

const MOCK_DIR = '../mock-api';
const PORT = 9000;

const ANSI_RED = '\x1b[31m';
const ANSI_YELLOW = '\x1b[33m';
const ANSI_RESET = '\x1b[0m';
const ANSI_CYAN = '\x1b[36m';
const ANSI_GREEN = '\x1b[32m';

const app = express();

app.use(cors());
app.use((req, res, next) => {
  const pathWithoutHost = url.parse(req.url).path;
  const pathWithoutParams = pathWithoutHost.split('?')[0].replace(/\/$/, ''); 
  const mockupFilePathWithMethod = `${MOCK_DIR}${pathWithoutParams}/${req.method}.json`;//.
  let mockupFilePath = mockupFilePathWithMethod;
  const statusCode = 200;
  try {
    if(!fs.existsSync(mockupFilePathWithMethod)) { 
      mockupFilePath = `${MOCK_DIR}${pathWithoutParams}.json`;
    }
    const json = fs.readFileSync(mockupFilePath, 'utf8');
    let serverResponse = json;
    console.log(`${getFormattedTime()} ${ANSI_CYAN}${req.method}${ANSI_RESET} ${pathWithoutParams}  ${ANSI_GREEN} ${mockupFilePath}`); 
    res.write(serverResponse);
    res.header('Access-Control-Allow-Origin','*');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
    res.header('Access-Control-Allow-Credentials', true);
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH');
    next();
  }
  catch (e) {
   console.log ('ERROR' + e);
   console.log(`${getFormattedTime()} ${ANSI_RED}${req.method}${ANSI_RESET} ${pathWithoutParams} (${mockupFilePath})`);
   res.writeHead(400);
 } finally { 
   res.end();
 } 
  });
app.use('/api', apiMocker('mock-api'));
console.log(`Mock API Server is up and running at: http://localhost:${PORT}`);
app.listen(PORT);
console.log(`${getFormattedTime()} Running JSON API ${MOCK_DIR} server on port ${PORT}`);

function getFormattedTime() {
  const date = new Date();
  const time = `[${pad(date.getHours())}:${pad(date.getMinutes())}]`;
  return ANSI_YELLOW + time + ANSI_RESET;
}

function pad(n) {
  return `0${n}`.slice(-2);
}