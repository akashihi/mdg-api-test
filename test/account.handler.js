const pactum = require('pactum');

pactum.handler.addSpecHandler('Create Account', (ctx) => {
  const { spec, data } = ctx;
  spec.post('/account');
  spec.withJson(data);
  spec.use('create');
});

pactum.handler.addSpecHandler('Validate account balance', (ctx) => {
  const { spec, data } = ctx;
  const { id, balance } = data;
  spec.use('read');
  spec.get('/account/{id}');
  spec.withPathParams('id', id);
  spec.expectJson('data.attributes.balance', balance);
});
