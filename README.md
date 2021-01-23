# Aliexpress

Aliexpress bonus buddies bot - Linux & Windows

# LEGGI TUTTO PRIMA DI INIZIARE

## Setup 
##### Sviluppatori:
Per poter sviluppare su questo branch è necessario:
 1) Installare l'ultima versione di [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
 2) Installare la [JDK 15](https://adoptopenjdk.net/) 
 3) Installare l'ultima versione di [Apache Maven](https://maven.apache.org/download.cgi)
 4) Abilitare le preview features in IntelliJ IDEA
 5) Scaricare il plugin Lombok da IntelliJ IDEA
 6) Abilitare l'annotation processing da IntelliJ IDEA nelle impostazioni
 7) Assicurarsi di syncare le dipendenze andando nel POM e clickando [sull'icona di Maven](https://imgur.com/a/0eLWHfT) in alto se presente sennò salta questo passaggio
 8) Un dispositivo Android con una SIM il cui piano tariffario comprende i dati mobili

## Per iniziare
Si consiglia di creare max due o tre accounts aliexpress con indirizzi mail reali in questo modo:
1. Apri una nuova finestra in incognito
2. Apri i dev tools clickando su ispeziona elemento, poi in alto clicka sull'icona di un monitor e di un telefono alla sinistra della scritta Elemento
3. Seleziona Pixel 2 dal menu a tendina in alto al centro
4. Recati su aliexpress.com e fai il login
5. Recati [qui](https://campaign.aliexpress.com/wow/gf/cashdailyoutc/index?_addShare=no)
6. Clicka avanti, infine clicka su condividi, copia link e incollalo da qualche parte
7. Chiudi la pagina
8. Ripeti per ogni account

## Setup dispositivo android
##### Dispositivi non-rooted:
1) Avvia una volta l'applicazione per generare i file di default che si trovano nella cartella C:\Users\<Utente>\aliexpress per windows e /home/aliexpress per Linux
2) Disattiva notifiche e sospensione dello schermo
3) Vai nelle impostazioni e attiva le [impostazioni da sviluppatore](https://www.wikihow.it/Abilitare-le-%27Opzioni-sviluppatore%27-su-Android)
4) Abilita il Tethering USB, Debug USB e Posizione Puntatore
5) Nelle impostazioni trova il pulsante per la modalità aereo e segnati le coordinate
6) Disattiva Posizione Puntatore
7) Apri il file ip.bat nella cartella resources, cancella tutto e incolla:
   ```
   adb shell input tap x y
   ping 127.0.0.1 -n 6 > nul
   adb shell input tap x y
   ```
   dove x e y sono le coordinate precedentemente annotate
   
##### Dispositivi rooted:
1) Avvia una volta l'applicazione per generare i file di default che si trovano nella cartella C:\Users\<Utente>\aliexpress per windows e /home/aliexpress per Linux
2) Vai nelle impostazioni e attiva le [impostazioni da sviluppatore](https://www.wikihow.it/Abilitare-le-%27Opzioni-sviluppatore%27-su-Android)
4) Abilita il Tethering USB e il Debug USB
5) Apri il file ip.bat nella cartella resources, cancella tutto e incolla:
   ```
   adb root

   adb shell settings put global airplane_mode_on 1
   adb shell am broadcast -a android.intent.action.AIRPLANE_MODE

   ping 127.0.0.1 -n 6 > nul

   adb shell settings put global airplane_mode_on 0
   adb shell am broadcast -a android.intent.action.AIRPLANE_MODE
   ```

## Run with intelliJ Idea
1) Apri il progetto con intelliJ Idea
2) Apri la classe AliexpressApplication
3) Build -> build project
4) Build -> recompile AliexpressApplication.java
5) Run -> edit configurations
6) \+ -> Application
7) <a href="https://ibb.co/JHQshWj"><img src="https://i.ibb.co/CQ2HrpK/abb.png" alt="abb" border="0"></a>
8) Applica
9) Ok
10) Run -> run
11) Tutte le configurazioni presenti nella cartella resources sono di default, quelle utilizzate dall'applicazione vengono salvate nella cartella C:\Users\<Utente>\aliexpress per windows e /home/aliexpress per Linux
12) SOLO SU LINUX: `chmod +x stop.sh; chmod +x chromedriver; chmod +x ip.sh`
13) config.properties -> 
```
links=[firstlink],[secondlink] // il link in ogni array riceve il primo click, il secondo link nello stesso array riceve il secondo link etc... !i link devono essere tutti diversi
done_limit=0 // se diverso da 0 si ferma al numero di click effettuati che si ha impostato
error_limit=0 // se diverso da 0 si ferma al numero di errori generati che si ha impostato
password=dsf4576sre78#33S239 // password per gli account fake
adblocker=true
headless=false
profile_caching=true
ship_to=true
reverse=false
tg_chatId= // non usare, era una feature che trovi nel branch old
tg_botToken= // non usare, era una feature che trovi nel branch old
user_agent="Mozilla/5.0 (Linux; Android 6.0.1; RedMi Note 5 Build/RB3N5C; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/68.0.3440.91 Mobile Safari/537.36"
```
14) Run -> run

## Run with maven
1) `mvn clean`
2) `mvn package`
3) `java -jar target/abb-bot-1.0-SNAPSHOT.jar`
4) Tutte le configurazioni presenti nella cartella resources sono di default, quelle utilizzate dall'applicazione vengono salvate nella cartella C:\Users\<Utente>\aliexpress per windows e /home/aliexpress per Linux
5) SOLO SU LINUX: `chmod +x stop.sh; chmod +x chromedriver; chmod +x ip.sh`
6) config.properties -> 
```
links=[firstlink],[secondlink] // il link in ogni array riceve il primo click, il secondo link nello stesso array riceve il secondo link etc... !i link devono essere tutti diversi
done_limit=0 // se diverso da 0 si ferma al numero di click effettuati che si ha impostato
error_limit=0 // se diverso da 0 si ferma al numero di errori generati che si ha impostato
password=dsf4576sre78#33S239 // password per gli account fake
adblocker=true
headless=false
profile_caching=true
ship_to=true
reverse=false
tg_chatId= // non usare, era una feature che trovi nel branch old
tg_botToken= // non usare, era una feature che trovi nel branch old
user_agent="Mozilla/5.0 (Linux; Android 6.0.1; RedMi Note 5 Build/RB3N5C; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/68.0.3440.91 Mobile Safari/537.36"
```
7) `java -jar target/abb-bot-1.0-SNAPSHOT.jar`
