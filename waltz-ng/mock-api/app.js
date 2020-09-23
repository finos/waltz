const express = require('express');
const nodeurl = require('url');
const apiMocker = require('connect-api-mocker');
const cors = require('cors');
const PORT = 9000;
const app = express();
app.use(cors());
app.use((req, res, next) => {
  /* let url = nodeurl.parse(req.url).pathname;
  let q = nodeurl.parse(req.url).query;
  if(q)  q ='/'+q.replace('=','/');
  if(q && url.split('/')[url.split('/').length-1]==q.split('/')[1]){
    let temp = url.split('/');
    temp.pop();temp.join('/');
    url=temp+q;
  }else if(q){
    url+=q;
  }
  console.debug('url: ',url);
  nodeurl.parse(req.url).pathname = url; */
    res.header('Access-Control-Allow-Origin','*');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
    res.header('Access-Control-Allow-Credentials', true);
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH');
    next();
  });
app.use('/api', apiMocker('mock-api'));
console.log(`Mock API Server is up and running at: http://localhost:${PORT}`);
app.listen(PORT);
