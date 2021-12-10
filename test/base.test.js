const { request } = require('pactum');

// load handlers
require('./error.handler');
require('./op.handler');

// global hook
before(() => {
    request.setBaseUrl('http://localhost:9000/api');
});