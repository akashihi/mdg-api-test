const pactum = require("pactum");

pactum.handler.addSpecHandler('Create Account', (ctx) => {
    const {spec, data} = ctx;
    spec.post('/account')
    spec.withJson(data)
    spec.use('create')
})