### TODO
* uwspólnić build process dla serwisów (żeby podmieniać tylko os, a kod budować raz)
* pozbyć się redundancji w konfigu (podawanie zahardkodowanych zmiennych)


### Uruchamianie
* `docker-compose up` - uruchamia nginx i serwisy
* `./gradlew daemon:bootrun` - uruchamia proces daemona (kontrolera)
* docelowo napisze się jakiś krótki `run.sh`, ale na razie do devu niech będzie ręcznie, bo czasami coś trzeba zdebugować