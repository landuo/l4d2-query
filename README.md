
# l4d2-query
Use rcon and a2s protocol to query L4D2 server info.

## Send command to l4d2 server
```Java
RconRequest request = new RconRequest();
request.setIp("10.211.55.3");
request.setPort(27015);
request.setPasswd("password");
request.setCmd("status");
String response = RconUtil.send(request);
```
and get result
```shell
hostname: Left 4 Dead 2 Server
version : 2.2.2.6 8776 secure  (unknown)
udp/ip  : 10.211.55.3:27015 [ public x.x.x.x:27015 ]
os      : Windows Dedicated
map     : c2m1_highway
players : 0 humans, 0 bots (4 max) (hibernating) (unreserved)

# userid name uniqueid connected ping loss state rate adr
#end
```

## Retrieves information about the server 
```Java
System.out.println(A2sUtil.getA2sInfo("10.211.55.3:27015"));
```
and get server info  

```SourceServerInfo(id=550, name=Left 4 Dead 2 Server, map=c2m1_highway, folder=left4dead2, game=Left 4 Dead 2, players=0, maxPlayers=4, bots=0, serverType=d, environment=Windows, visibility=public, vac=secured, version=2.2.2.6, port=27015, steamID=90167827942855688, sourceTVPort=null, sourceTVName=null, keywords=coop,empty,secure, gameID=550, times=3)```

## retrieves information about the players currently on the server
```Java
System.out.println(A2sUtil.getPlayers("x.x.x.x:27015"));
```
and get server info  

```A2sPlayers(total=17, players=[Player(index=0, name=The Last one, score=44, duration=59m48s), Player(index=0, name=King, score=35, duration=1h19m15s), Player(index=0, name=Yulie, score=28, duration=1h26m32s), Player(index=0, name=Fast Rivals35, score=23, duration=40m7s), Player(index=0, name=Nick0331, score=22, duration=2h9m30s), Player(index=0, name=Sandeep, score=22, duration=1h51m1s), Player(index=0, name=Valerie, score=13, duration=44m50s), Player(index=0, name=NightCupNoodle, score=12, duration=1h22m36s), Player(index=0, name=mashedpotato, score=12, duration=1h5m11s), Player(index=0, name=Red, score=10, duration=1h2m10s), Player(index=0, name=HERBuk, score=9, duration=10m41s), Player(index=0, name=Đen, score=8, duration=2h9m36s), Player(index=0, name=Basil, score=7, duration=1h16m40s), Player(index=0, name=NeKogic芒果, score=6, duration=55m39s), Player(index=0, name=Cocomiel, score=6, duration=8m27s), Player(index=0, name=Halcyon, score=0, duration=4m16s), Player(index=0, name=TE MATO Y ME ESCAPO, score=-1, duration=7m49s)])```  

## Reference

[how_to_sign_and_release_to_the_central_repository_with_github_actions.md](https://gist.github.com/sualeh/ae78dc16123899d7942bc38baba5203c)
