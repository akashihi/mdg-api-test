const { addSpecHandler } = require('pactum').handler;

addSpecHandler('expect success', (ctx) => {
  const { spec, data } = ctx;
  spec.withHeaders('Content-Type', 'application/vnd.mdg+json')
  spec.expectStatus(data);
  spec.expectHeader('content-type', 'application/vnd.mdg+json');
});

addSpecHandler('create', (ctx) => {
  const { spec } = ctx;
  spec.use('expect success', 201);
});

addSpecHandler('read', (ctx) => {
  const { spec } = ctx;
  spec.use('expect success', 200);
});

addSpecHandler('update', (ctx) => {
  const { spec } = ctx;
  spec.use('expect success', 202);
});

addSpecHandler('delete', (ctx) => {
  const { spec } = ctx;
  spec.expectStatus(204);
});
