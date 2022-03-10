# lnd-manageJ

## What is the purpose of lnd-manageJ?

In the far future, you can run lnd-manageJ in the background and constantly gather information from your
[lnd](https://github.com/lightningnetwork/lnd) node.
This information is condensed and analyzed so that

- you can see your node's and each peer's/channel's forwarding activity
- you can understand each channel's/peer's forwarding characteristics (is it a sink? a source? bidirectional?)
- you can tweak the forwarding fees based on previous and forecasted routing activity

In short, lnd-manageJ helps you understand and manage the inner workings of your lnd node.


## Privacy and external API usage
This service requests transaction details from public APIs (Bitaps and Blockcypher).
As such, information about the channels associated with your node are leaked, possibly revealing your IP address and
enabling anti-privacy analyses.

Depending on the size of your node, you might run into limits imposed by these API providers.
The downloads will be retried automatically as long as the service runs, but the returned
information may be incomplete or delayed until the necessary downloads are completed.

**Do not run the service if any of this is an issue to you!**

## What can you do with lnd-manageJ?

Once the service is running, it offers several REST endpoints that can be accessed with, for example, `curl`.
All endpoints are served at the base URL `http://localhost:8081/`.
As an example, to get a list of all open channels, you can run:

```shell
curl -s http://localhost:8081/api/status/open-channels
```

As the JSON output isn't exactly nice to read, you might want to install `jq` and run:

```shell
curl -s http://localhost:8081/api/status/open-channels | jq
```

The service itself only provides metrics and error logs as its output.

### Endpoints

Status: `/api/status/` followed by...

 * `synced-to-chain`: `true` or `false` as returned by lnd (`getinfo`)
   * This can be used as a quick health check, as most of the information is rather useless if `lnd` is not working as expected.
   * Note that `false` is also returned if lnd is unreachable.
 * `block-height`: the current block height as returned by lnd (`getinfo`)
 * `open-channels`: the channel IDs of all open channels
 * `open-channels/pubkeys`: the pubkeys of all peers with at least one open channel
 * `all-channels`: the channel IDs of all channels (open, closed, waiting close, ...)
   * "pending open channels" are not included, as these do not have an ID, yet!
 * `all-channels/pubkeys`: the pubkeys of all peers with at least one channel as defined above

Channel specific: `/api/channel/{ID}/` (where `{ID}` is the channel ID) followed by...
 * (nothing): basic channel information (open height, remote pubkey, capacity, status, ...)
   * For closed private channels the `private` field is only set to `true` if this information could be collected before the channel was closed (it is only available while the channel is still open)
 * `balance`: the channel balance (for local/remote each: balance, available, reserve)
 * `policies`: local and remote fee rate, base fee, and enabled boolean
 * `close-details` (only for closed channels): close initiator, close height, force boolean, breach boolean
 * `fee-report`: satoshis earned by forwarding payments leaving out through the channel (`earned`) or coming in from the channel (`sourced`)
 * `flow-report`: amounts flowing through the channel, broken down into different categories
 * `flow-report/last-days/{DAYS}`: as above, but limited to the last `{DAYS}` days
 * `open-costs`: the on-chain costs you paid to open the channel
 * `close-costs`: the on-chain costs you paid to close the channel
 * `sweep-costs`: the on-chain costs you paid to sweep funds after a force-close (this may be negative)
 * `rebalance-source-costs`: the fees you paid to rebalance the channel, taking funds out of it
 * `rebalance-source-amount`: the amount of funds you took out of the channel as part of rebalancing
 * `rebalance-target-costs`: the fees you paid to rebalance the channel, putting funds into it
 * `rebalance-target-amount`: the amount of funds you put into the channel as part of rebalancing
 * `rebalance-support-as-source-amount`: the amount of funds you took out of the channel as part of rebalancing another channel
 * `rebalance-support-as-target-amount`: the amount of funds you put into the channel as part of rebalancing another channel
 * `self-payments-from-channel`: a list of all self-payments taking funds out of the channel (including payment details and a summary)
 * `self-payments-to-channel`: a list of all self-payments putting funds into the channel (including payment details and a summary)
 * `warnings`: shows the following warnings, if applicable
   * "Channel balance ranged from `X`% to `Y`% in the past 14 days" (if `X` < 10 and `Y` > 90)
   * "Channel has accumulated `X` updates" (if `X` > 100,000)
 * `details`: all of the above

**Note**: the channel ID can be supplied in any of the following formats:
 - `123456:123:1` (lnd specific compact format)
 - `123456x123x1` (compact format)
 - `135741307526774785` (short ID format)
 - `1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef:1` (outpoint)

Node specific: `/api/node/{PUBKEY}/` (where `{PUBKEY}` is the node pubkey) followed by...

 * `alias`: the node's alias (plain string, not JSON)
 * `open-channels`: channel IDs for all channels currently open with the peer
 * `all-channels`: channel IDs for all channels (open, closed, waiting close, ...) with the peer
   * "pending open channels" are not included, as these do not have an ID, yet!
 * `balance`: the aggregated channel balances (see channel endpoint)
 * `fee-report`: the aggregated fee report (see channel endpoint)
 * `fee-report/last-days/{DAYS}`: as above, but limited to the last `{DAYS}` days
 * `flow-report`: the aggregated flow report (see channel endpoint)
 * `flow-report/last-days/{DAYS}`: as above, but limited to the last `{DAYS}` days
 * `on-chain-costs`: aggregated on-chain costs (see channel endpoints `open-costs`, `close-costs`, `sweep-costs`)
 * `rebalance-source-costs`: aggregated costs (see channel endpoint)
 * `rebalance-source-amount`: aggregated amount (see channel endpoint)
 * `rebalance-target-costs`: aggregated costs (see channel endpoint)
 * `rebalance-target-amount`: aggregated amount (see channel endpoint)
 * `rebalance-support-as-source-amount`: aggregated amount (see channel endpoint)
 * `rebalance-support-as-target-amount`: aggregated amount (see channel endpoint)
 * `self-payments-from-peer`: aggregated list (see channel endpoint `self-payments-from-channel`)
 * `self-payments-to-peer`: aggregated list (see channel endpoint `self-payments-to-channel`)
 * `warnings`: shows the following warnings, if applicable
   * "No flow in the past `X` days" (if `X` >= 30)
   * "Node has been online `X`% in the past 7 days" (if `X` < 80)
   * "Node changed between online and offline `X` times in the past 7 days" (if `X` > 50)
 * `details`: all of the above

Warnings:

`/api/warnings`: show all warnings for all peers and channels

Legacy:

`/legacy/open-channels/pretty`: a readable list (not JSON!) of all open channels showing channel ID, pubkey, capacity and alias.
As the name implies, this endpoint will be removed sooner rather than later.

## Caching and Performance
Most information is cached to improve performance, which is why you might see outdated information.
Some information like node aliases does not need to be up-to-date and, as such, is cached for several minutes.
On the other hand, channel balance information is only cached for less than a second.

This project has not been tested in many environments.
Depending on your setup, it might be very slow and/or consume many resources.
Please share your insights by raising a GitHub issue!

As most information is cached, performance improves the more you use the endpoints.
However, sometimes (for example directly after starting the service) many pieces of information need to be gathered
from different sources, which may take several seconds. For example, transaction details for all (including closed)
channels need to be downloaded once (see below), which will delay any endpoint requesting information about a/all
channel(s).

## Is there a graphical user interface (GUI)?

No. If you're interested in creating one, please get in touch or just create a pull request!
If you have ideas for API improvements, please raise issues in GitHub.

## How can I run lnd-manageJ?

Install PostgreSQL (Debian: `apt install postgresql`) and create a database named `lndmanagej` and a user `bitcoin`.
You can tweak these settings in `application/src/main/resources/application.properties`.
Configure the database so that the user `bitcoin` can access `lndmanagej` without a password at `jdbc:postgresql://localhost:5432/lndmanagej` (`pg_hba.conf`: `local lndmanagej bitcoin trust`).

Install Java 17 and run `./start.sh`.
The service is intended to run 24/7 to collect statistics about your node.
You may restart both the service and lnd at any time.

## Disclaimer
This project is not related to bitromortac's Python based [lndmanage](https://github.com/bitromortac/lndmanage).

## Can I help?

Sure! Please test the software, review the code, create feature requests, report bugs, ...

## How can I reach you?
I'm in the [lnd slack](https://dev.lightning.community/) and on [Twitter](https://twitter.com/c_otto83).