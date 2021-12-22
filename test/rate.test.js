const pactum = require('pactum');
const { like } = require('pactum-matchers');
const itParam = require('mocha-param');

const USD = 840;
const EUR = 978;
const INR = 356;
const HUF = 348;

it('Load list of rates', async () => {
  await pactum.spec('read')
    .get('/rate/{ts}')
    .withPathParams('ts', '2017-09-20T13:29:00')
    .expectJsonMatch('data[0]', {
      type: 'rate',
      id: like(1),
      attributes: {
        from_currency: EUR,
        to_currency: USD,
        rate: 1.19
      }
    });
});

const RATES = [
  {
    ratename: 'existing',
    from: EUR,
    to: USD,
    rate: 1.19
  },
  {
    ratename: 'non-existing',
    from: INR,
    to: HUF,
    rate: 1
  }
];

itParam('Check ${value.ratename} rate value', RATES, async (params) => { // eslint-disable-line no-template-curly-in-string
  await pactum.spec('read')
    .get('/rate/{ts}/{from}/{to}')
    .withPathParams('ts', '2017-09-20T13:29:00')
    .withPathParams('from', params.from)
    .withPathParams('to', params.to)
    .expectJsonMatch('data', {
      type: 'rate',
      id: like(1),
      attributes: {
        from_currency: params.from,
        to_currency: params.to,
        rate: params.rate
      }
    });
});
