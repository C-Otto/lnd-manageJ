# Rating

The rating given for an individual channel or peer is designed to help compare a channel/node with respect to the
other channels/nodes on your peer.

## Computation
The higher the rating, the better the channel/peer. The following values are taken into account to compute the final
rating:

1. Received Payments

   For each mSAT received via a (non-self) payment via the channel/peer, the rating is increased by 1.
2. Earned fees

   For each mSAT earned by routing out via the channel/peer, the rating is increased by 1. 
3. Sourced fees

   For each mSAT earned by sats coming in from the channel/peer, but going out via some OTHER peer, the rating is increased by 1.
4. Rebalance Support (source)

   For each 10,000 mSAT taken from the local balance as part of a rebalance transaction (increasing the local balance to some other peer), the rating is increased by 1.
5. Rebalance Support (target)

   For each 10,000 mSAT received as part of a rebalance transaction (increasing the remote balance of some other peer), the rating is increased by 1.
6. Potential earnings

   For each 10 mSAT that could be earned by routing the local balance at the current fee rate, the rating is increased by 1. 

After summing up the values explained above, the final rating is determined by
 - dividing the value by the number of analyzed days, then
 - dividing the value by the average local balance (normalized to 1 million satoshis, i.e. the value is
divided by 10 for an average local balance of 10M satoshis, and the value is multiplied by 2 for an average local balance of 500,000 satoshis).

As such, when considering 30 days for the analysis, a rating of 1,000 could mean:

- the channel/peer earned 30 satoshis with an average local balance of 1M sat, and currently no local balance
- you received a payment worth 30 satoshis via the channel/peer with an average local balance of 1M sat, and currently no local balance
- the channel/peer earned 30 satoshis with an average local balance of 1M sat, a current local balance of 10M sat, and a fee rate of 0pm
- the channel/peer earned 0 satoshis with an average local balance of 1M sat, a current local balance of 10M sat, and a fee rate of 30pm
- the channel/peer earned 0 satoshis with an average local balance of 1M sat, a current local balance of 5M sat, and a fee rate of 60pm
- the channel/peer earned 0 satoshis with an average local balance of 10M sat, a current local balance of 1M sat, and a fee rate of 3,000pm
- the channel/peer earned 750 satoshi with an average local balance of 25M sat, has a local balance of 25M sat, and a fee rate of 0ppm
- the channel/peer earned 15 satoshis, generated earnings in other channels worth 15 sat, with an average local balance of 1M sat, has a local balance of 1M sat, and a fee rate of 0ppm
- the channel/peer earned 15 satoshis, with an average local balance of 1M sat, sent out 150,000 sat to help rebalance other channels, and currently no local balance
- ...

## Warning
A warning is shown for nodes which have a rating below the configured threshold.
The default threshold is 1,000, which can be changed via `node_rating_treshold=` in the `[warnings]` section of the
configuration file.

## Configuration

Using `minimum_age_in_days=` in the `[ratings]` section you can configure how old a channel needs to be before it is considered for the analysis (default: 30). 
Using `days_for_analysis=` in the `[ratings]` section you can configure the number of days used for the analysis (default: 30).
