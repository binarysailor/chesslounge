## API

### WebSockets

#### Connecting

`/house` - the WS URL where the clients should connect

#### Generic client message shape
```
{
    "messageId": <uuid>
    "message": <string>
}
```    

eg.
```
{
    "messageId": ""
    "message": "game 874d-2833-....-002c move e2-e4"
}
```


##### Seeking a game

Client: `seekplay` 

Server: 

```
{
    "type": "SEEK_RESPONSE",
    "seekId": <UUID>,
    "ok": <boolean>,
    "message": <string> (optional)
}

```

```
{
    "type": "GAME_STARTED",
    "seekId": <UUID>, (optional - only sent when addressing one of the players who are playing and were seeking the game; for example: spectators are not going to receive it)
    "gameId": <UUID>,
    "white": {
        "id": <UUID>,
        "name": <string>
    },
    "black": {
        "id": <UUID>,
        "name": <string>
    }
}
```

##### Playing a game

Client: 
```
"message": "game <gameId> move <string>"
```

eg.
```
"message": "game 810ee22c-d018-4cad-b6d6-48cf907caab7 move e2-e4"
```

Castles are represented as king movements (eg. `move e1-g1`). Pawn promotions - TBD.

Server:

###### move accepted
Content:
```
{
    "type": "MOVE_RESPONSE",
    "correlationId": <uuid>
    "gameId": <uuid>,
    "ok": true,
    "moveNumber": <int>,
    "symbol": <string>
    "newPosition": <string> // FEN
}
```
Recipients:
players, observers

###### illegal move
Content:
```
{
    "type": "MOVE_RESPONSE",
    "correlationId": <uuid>
    "gameId": <uuid>,
    "ok": false,
    "message": <string>,
    "moveNumber": <int>,
    "symbol": <string>
}
```
Recipients:
requestor

### REST

