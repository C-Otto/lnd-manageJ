# #PickhardtPayments

## Work in progress!

See https://arxiv.org/abs/2107.05322.
Please reach out to me on Twitter (https://twitter.com/c_otto83) to discuss more about this!

The implementation is based on the piecewise linearization approach:
https://lists.linuxfoundation.org/pipermail/lightning-dev/2022-March/003510.html. 
There is also a lightweight python package being developed which can be used for simulations or to do production tests at: https://github.com/renepickhardt/pickhardtpayments 

# Requirements
1. Currently (as of v0.15.0-beta, July 2022) lnd does not allow sending a replacement shard once a shard of an active MPP
   fails. This, sadly, is necessary to complete MPPs that regularly run into temporary channel failures due to lack of
   funds. This is fixed with https://github.com/lightningnetwork/lnd/issues/5746, scheduled for lnd v0.15.1-beta.
2. The graph algorithm implementation used to do the heavy lifting currently is only supported for amd64 (x86_64) on
   Linux, Windows, and Mac systems. See https://github.com/C-Otto/lnd-manageJ/issues/13.
3. You need to enable middleware support in lnd: add a section `[rpcmiddleware]` with `rpcmiddleware.enable=true` to 
   your `lnd.conf` and restart lnd. Once enabled, lnd-manageJ will spy on every RPC request and
   response, without changing/blocking any of the data. However, despite the read-only configuration, requests may
   fail because of this if lnd-manageJ does not respond in time (crash, shutdown, ...).
   See https://github.com/lightningnetwork/lnd/issues/6409. To mitigate this risk, you can add
   `rpcmiddleware.intercepttimeout=10s` to the same section (the default is 2s).

# Payment Options
The following endpoints allow you to specify payment options that can be provided as the body of an HTTP `POST` request.
Note that the content type of the request must be set to `application/json`.

Example body:
```
{
    "feeRateWeight": 0,
    "feeRateLimit": 123,
    "ignoreFeesForOwnChannels": true
}
```

One component of this is the fee rate weight, explained below.
The fee rate limit is optional.
If set, channels with a fee rate of this value or higher are not considered for the payment.
Only routes with a fee rate below this limit are attempted.

If `ignoreFeesForOwnChannels` is set to false, fee rates configured for your own channels are considered as costs,
even though you don't have to pay those fees.

## Fee Rate Weight
The default fee rate weight is 0, which optimizes the computation for reliability and ignores fees.

Any value > 0 takes fees into account. Pick higher fee rate weights to compute cheaper routes.
 Note that the probability is still taken into account, even with high fee rate weights. As such, a massive channel
 may be picked, even though it charges a high fee rate.

A value of 1 seems to be a good compromise (using the default quantization value).

# Configuration options
You can configure the following values in the `[pickhardt-payments]` section of your `~/.config/lnd-manageJ.conf`
configuration file:

* `liquidity_information_max_age_in_seconds` (default 600, 10 minutes):
  * lower/upper bound information observed from payment failures are only kept this long
  * this information is kept for each pair of peers
  * once any value (lower bound, upper bound, amount in-flight) is updated, the "age" is reset
* `use_mission_control` (default: false)
  * regularly augment upper bound information based on information provided by lnd, as part of "mission control"
  * this is not as helpful, as lnd-manageJ collects the same information in real-time
* `quantization` (default 10000, in satoshis):
  * only consider payment shards with a multiple of this number to lower computational complexity: when sending 20k
    sat with a quantization of 10k sat, either one shard worth 20k sat is attempted, or two shards worth 10k
  * when sending amounts lower than the configured quantization, the amount itself is used as the quantization
  * even if the amount you try to send is not divisible by the configured quantization, the resulting MPP still covers
    the whole amount 
* `piecewise_linear_approximations` (default: 5):
  * this corresponds to `N` in the paper

# MPP computation

You can compute an MPP based on #PickhardtPayments using any of the following endpoints:

* HTTP `POST`: `/api/payments/from/{source}/to/{target}/amount/{amount}`
  * compute an MPP from the given node `source` to the given node `target` using the given payment options
  * the amount is given in satoshis
* HTTP `GET`: `/api/payments/from/{source}/to/{target}/amount/{amount}`
  * as above, with default payment options (fee rate weight 0)
* HTTP `POST`: `/api/payments/to/{pubkey}/amount/{amount}`
  * originate payments from the own node using the given payment options
* HTTP `GET`: `/api/payments/to/{pubkey}/amount/{amount}`
  * as above, with default payment options (fee rate weight 0)

# Paying invoices

* HTTP `POST`: `/api/payments/pay-payment-request/{paymentRequest}`
  * Pay the given payment request (also known as invoice) using the given payment options (fee rate weight etc.)
* HTTP `GET`: `/api/payments/pay-payment-request/{paymentRequest}`
  * as above, with default payment options (fee rate weight 0)

The response shows a somewhat readable representation of the payment progress, including the final result.

# Top Up

* HTTP `GET`: `/api/payments/top-up/{pubkey}/amount/{amount}`
  * Sends satoshis out via some channel and back to the own node through the specified peer so that the local balance
    to that peer is increased.
  * The given amount is the the local balance you'd like to have *after* the payment is done.
  * If you have more than one channel to the peer, the target amount is the sum of the (available) local balances.
  * If the local balance to that peer is more than the given amount, nothing is done.
  * If the difference between the current local balance and the target amount is less than the configured threshold
    (see below), nothing is done. 
  * The payment is only attempted for routes that make sense from an economic perspective. If you try to top up the
    channel(s) to node Z...
    * ...and if one of the routes is supposed to leave via a channel to node A, the fee rate towards node A must be
      less than the fee rate towards node Z.
    * ...and a route found by the algorithm costs more (in ppm) than the fee rate difference between the channels to
      node Z and node A, the whole payment fails (it is not attempted).
  * Invoices (payment requests) created for top-up payments expiry after 30 minutes. This value can be configured as
    `expiry_seconds=`.
* HTTP `POST`: `/api/payments/top-up/{pubkey}/amount/{amount}`
  * allows you to lower the fee rate limit (values higher than the computed fee rate limit are ignored)
  * allows you to specify a different fee rate weight
  * The value provided as `ignoreFeesForOwnChannels` is ignored, for top-up such fees are never ignored

The threshold, i.e. the minimum difference between the current local balance and the requested amount, defaults to
10,000sat. You can configure this value by setting `threshold_sat=` in the configuration file.

As before, the response shows a somewhat readable representation of the payment progress, including the final result.
