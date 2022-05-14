# #PickhardtPayments

## Work in progress!

See https://arxiv.org/abs/2107.05322.
Please reach out to me on Twitter (https://twitter.com/c_otto83) to discuss more about this!

The implementation is based on the piecewise linearization approach:
https://lists.linuxfoundation.org/pipermail/lightning-dev/2022-March/003510.html.

# Requirements
1. Currently (as of v0.14.3-beta, May 2022) lnd does not allow sending a replacement shard once a shard of an active MPP
   fails. This, sadly, is necessary to complete MPPs that regularly run into temporary channel failures due to lack of
   funds. See https://github.com/lightningnetwork/lnd/issues/5746 for a (possible) fix. You might want to stick to
   testnet until this is properly fixed!
2. The graph algorithm implementation used to do the heavy lifting currently is only supported for amd64 (x86_64) on
   Linux, Windows, and Mac systems. See https://github.com/C-Otto/lnd-manageJ/issues/13.
3. You need to enable middleware support in lnd: add a section `[rpcmiddleware]` with `rpcmiddleware.enable=true` to 
   your `lnd.conf`, restart lnd and restart lnd-manageJ. Once enabled, lnd-manageJ will spy on every RPC request and
   response, without changing/blocking any of the data. However, despite the read-only configuration, requests may
   fail because of this if lnd-manageJ does not respond in time (crash, shutdown, ...).
   See https://github.com/lightningnetwork/lnd/issues/6409.

# Fee Rate Weight
The following endpoints allow you to specify a fee rate weight.
The default fee rate weight is 0, which optimizes the computation for reliability and ignores fees.

Any value > 0 takes fees into account. Pick higher fee rate weights to compute cheaper routes.
 Note that the probability is still taken into account, even with high fee rate weights. As such, a massive channel
 may be picked, even though it charges a high fee rate.

A value of 1 seems to be a good compromise (using the default quantization value)

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

* `/beta/pickhardt-payments/from/{source}/to/{target}/amount/{amount}/fee-rate-weight/{feeRateWeight}`
  * compute an MPP from the given node `source` to the given node `target`
* `/beta/pickhardt-payments/from/{source}/to/{target}/amount/{amount}`
  * as above, with default fee rate weight 0
* `/beta/pickhardt-payments/to/{pubkey}/amount/{amount}/fee-rate-weight/{feeRateWeight}`
  * originate payments from the own node
* `/beta/pickhardt-payments/to/{pubkey}/amount/{amount}`
  * as above, with default fee rate weight 0

# Paying invoices

Warning: Don't do this on mainnet, yet! This is very much work in progress.

* `/beta/pickhardt-payments/pay-payment-request/{paymentRequest}/fee-rate-weight/{feeRateWeight}`
  * Pay the given payment request (also known as invoice) using the configured fee rate weight.
* `/beta/pickhardt-payments/pay-payment-request/{paymentRequest}`
  * as above, with default fee rate weight 0

The response shows a somewhat readable representation of the payment progress, including the final result.
