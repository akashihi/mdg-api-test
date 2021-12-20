const pactum = require("pactum");

pactum.handler.addSpecHandler('Create Budget', (ctx) => {
    const {spec, data} = ctx;
    spec.post('/budget')
    spec.withJson(data)
    spec.use('create')
})
