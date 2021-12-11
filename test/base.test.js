const { request } = require('pactum');

// load handlers
require('./error.handler');
require('./op.handler');
require('./currency.handler');
require('./settings.handler');

// global hook
before(() => {
    request.setBaseUrl('http://localhost:9000/api');
});