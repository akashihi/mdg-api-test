const pactum = require("pactum");
pactum.handler.addSpecHandler('Set setting value', (ctx) => {
    const {spec, data} = ctx;
    const {id, value, setting} = data;
    spec.put('/setting/{id}');
    spec.withPathParams('id', id);
    spec.withJson({
        '@DATA:TEMPLATE@': setting,
        '@OVERRIDES@': {
            data: {
                attributes: {
                    value: value
                }
            }
        }
    })
    spec.expectJson('data.attributes.value', value);
    spec.use('update');
})

pactum.handler.addSpecHandler('Check setting value', (ctx) => {
    const {spec, data} = ctx;
    const {id, value} = data;
    spec.get('/setting/{id}');
    spec.withPathParams('id', id);
    spec.expectJson('data.attributes.value', value);
    spec.use('read');
})