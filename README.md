### Example DNAT/SNAT routing
```shell
sudo iptables \
--table nat \
--append PREROUTING \
--protocol ALL \
--destination 192.168.100.243 \
--jump DNAT \
--to-destination 192.168.100.244

sudo iptables \
--table nat \
--append POSTROUTING \
--protocol ALL \
--destination 192.168.100.244 \
--jump SNAT \
--to-source 192.168.100.243
```
#### Precondition
Host must have `iptables` installed and `ip_forwarding` enabled. Can be enabled by adding the following line
`net.ipv4.ip_forward=1` to `/etc/sysctl.conf` file.  (for the setting to take effect after appending file, run `sysctl -p`)
