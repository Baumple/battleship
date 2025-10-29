# Batteship
## Execute project:
- Server: `./gradlew :server:run --console=plain`
- Client: `./gradlew :client:run --console=plain --args=<player name>`

## Klassendiagramm (beinhaltet nicht alle Klassen in org.shared)

```mermaid
classDiagram
    namespace org.server.errorhandling {
        class Result["sealed interface Result (T, R)"] {
            +ok(value: T) Result.Ok(T, R)$
            +error(error: R) Result.Error(T, R)$

            +isError() boolean
            +isOk() boolean

            +then(f: fn(T) -> Result(S, R)) Result(S, R)
            +mapOk(f: fn(T) -> S) Result(S, R)
        }
        class Ok ["record Result.Ok(T, R)"] {
            value: T
        }
        class Error ["record Result.Error(T, R)"] {
            error: R
        }

        class ServerError["sealed interface ServerError"]

        class ServerError.InitError["record ServerError.InitError"] {
            exception: Exception
        }
        class ServerError.IOError["record ServerError.IOError"] {
            exception: IOException
        }
        class ServerError.ClientConnectError["record ServerError.ClientConnectError"] {
            exception: IOException
        }
        class ServerError.ClientPropertyError["record ServerError.ClientPropertyError"] {
            msg: String
        }
        class ServerError.InvalidHandshakeError["record ServerError.InvalidHandshakeError"]
        class ServerError.InvalidMoveReceived ["record ServerError.InvalidMoveReceived"] {
            exception: IllegalArgumentException
        }
    }

 
    namespace org.server {
        class ServerMain["Main"] {
            main(args: String[])
        }

        class GameServer {
            -socket: ServerSocket
            -player1: Player
            -player2: Player

            +start()
            -startGame() Result(Void, ServerError)
            -handleAttack(attacker: Player, dender: Player) Result(Void, ServerError)
            -handleGameEnd(winner: Player, loser: Player) Result(Void, ServerError)

            -initialize() Result(Void, ServerError)
            -connectPlayers() Result(Void, ServerError)
            -disconnectPlayers() Result(Void, ServerError)

            -close() throws IOException
        }

        class Player {
            -name:      String
            -socket:    Socket
            -reader:    BufferedReader
            -writer:    PrintWriter
            -shipBoard: ShipBoard

            -Player(socket: Socket) throws IOException
            -doHandshake() Result(Void, ServerError)
            -connect() Result(Void, ServerError)
            -readPlayerInfo() Result(Void, ServerError)

            +getMove() Result<Move, ServerError>
            +hasLost() boolean
            +sendBoardUpdate(m: Move) Result(Boolean, ServerError)
            +sendResponse(isHit: boolean)
            +sendAwaitMove()
            +sendDefeat()
            +sendWin()

            +fromSocket(socket: Socket) Result(Player, ServerError)
            +run()
            +close() throws IOException
        }
    }



    namespace org.client.errorhandling {
        class ClientError ["sealed interface ClientError"]
        class InitError ["record ClientError.InitError"] {
            e: IOException
        }
        class ServerConnectError ["record ClientError.ServerConnectError"] {
            e: IOException
        }
        class InvalidHandshakeError ["record ClientError.InvalidHandshakeError"]
        class UnknownInstructionError ["record ClientError.UnknownInstructionError"]
        class UserIOError ["record ClientError.UserIOError"]
        class IOError ["record ClientError.IOError"]
        class LostConnectionError ["record ClientError.LostConnectionError"]

        class ClientException {
            error: ClientError
            +ClientException(error: ClientError)
            +of(error: ClientError) ClientException$
        }
    }


    namespace org.client {
        class ClientMain["Main"] {
            main(args: String[])
        }

        class Marker["enum MarkerBoard.Marker"] {
            None,
            Miss,
            Hit
        }
        class MarkerBoard {
            -board: MarkerBoard.Marker[][]

            +MarkerBoard()
            +placeMarker(x, y: int, marker: MarkerBoard.Marker)
            +toString() String
        }

        class GameClient {
            -socket: Socket
            -reader: BufferedReader
            -writer: PrintWriter
            -scanner: Scanner
            -markerBoard: MarkerBoard
            
            +GameClient(name: String)
            +start(scanner: Scanner) throws ClientException
            -gameLoop() throws ClientException
            -handleDefeat()
            -handleAwaitMove()
            -handleWin()
            -handleSendMove() throws ClientException
            -receiveMoveResult() boolean throws ClientException
            -handleUpdateBoard() throws ClientException

            -promptMove() Move
            -readLineOrThrow() throws ClientException

            -connectToServer(board: ShipBoard) throws ClientException
            -disconnectFromServer() throws ClientException
            -performHandshake() throws ClientException
            
            +close() throws IOException
        }
    }

    namespace org.shared {
        class Move {
            x: int
            y: int
        }
        class ShipBoard {
            -ships: Ship[]

            -ShipBoard()

            +fromArray(Ship[] ships) throws IllegalArgumentException$
            +fromUserInput(scanner: Scanner)
            +registerHit(x, y: int) boolean
            +hasShipAt(x, y: int) boolean
            +getShipAt(x, y: int) Ship
            +hasAliveShips() boolean

            +encode() String
            +decode(encoded: String) throws IllegalArgumentException$

            +toString() String

            -hasShipAt(ships: Ship[], x, y: int) boolean$
            -hasOverlap(ships: Ship[], x, y, length: int, o: Ship.Orientation)$
            -hasOverlap(ships: Ship[], x, y, length: int, o: Ship.Orientation)$
            -isWithinBounds(Ship ship)$
            -isWithinBounds(x, y, length: int, o: Ship.Orientation)$
            -isValidShipArrayOrThrow(Ship[] ships) throws IllegalArgumentException$
            -promptOrientation() Ship.Orientation$
        }

        class Ship {
            -x, y, length: int
            -o: Orientation
            -segments: boolean[]

            +Ship(x, y, length: int, Ship.Orientation o)
            +registerHit(x, y: int) boolean
            +isAt(x, y: int) boolean
            +isAlive(x, y: int) boolean
            +getCellAt(x, y: int) char

            +getX() int
            +getY() int
            +getLength() int
            +getOrientation() Ship.Orientation


            +encode() String
            +decode(encoded: String) Ship throws IllegalArgumentException$
        }

        class Ship.Orientation["enum Ship.Orientation"] {
            Vertical
            Horizontal

            encode() char
        }

   
    }
    ShipBoard "1" *-- "5" Ship : has
    Ship "1" *-- "1" Ship.Orientation : has

    GameServer "2" *-- "1" Player : has
    GameServer ..> Result : uses
    GameServer ..> ServerError : uses
    
    Player ..> Move : uses
    Player "1" *-- "1" ShipBoard : has
    Player ..> Result : uses
    Player ..> ServerError : uses

    ClientException "n" *-- "1" ClientError : wraps
    ClientException <|-- Exception


    MarkerBoard "1" *-- "n" Marker : has

    Result <|-- Ok
    Result <| -- Error

    ServerMain ..> GameServer : uses
    ClientMain ..> GameClient : uses

    GameClient ..> ClientException : throws
    GameClient ..> Move : uses
    GameClient "1" *.. "1" MarkerBoard : has
    GameClient ..> ShipBoard : uses

    ClientError <|-- InitError
    ClientError <|-- ServerConnectError
    ClientError <|-- InvalidHandshakeError
    ClientError <|-- UnknownInstructionError
    ClientError <|-- UserIOError
    ClientError <|-- IOError
    ClientError <|-- LostConnectionError

       ServerError <|-- ServerError.InitError
    ServerError <|-- ServerError.IOError
    ServerError <|-- ServerError.ClientConnectError
    ServerError <|-- ServerError.ClientPropertyError
    ServerError <|-- ServerError.InvalidHandshakeError
    ServerError <|-- ServerError.InvalidMoveReceived
    
```
[mermaid.live link]([https://mermaid.live/edit#pako:eNq1Gttu2zb0VwQhD_JmG3GaLI1QFEjTbi2QzEWcttviPLASbROWSYOUc2mXf98hKckUSSny0uohlnTuh-dGKt_DhKU4jMMkQ0K8JWjO0WpKpzSAi6IVFmuU4IDx-VBgfov5EHPO-ALRNCN0HnzXmPJSHIJLLDZZfj0NBUYZTgNCc8xnkoeGBNFVP7jsTcMbk1Zev7JldIuyDY6Dq16BPRwvNf6ehau0iNTfGMAl-jv1uqSwaIjQ4F7wlbEMIxcO0gygBc4XmEazOJjRCPQbvC5kRhMprVd7sihXaA2cTdJJE_6j7c_xMgBncpwwntpO8TixdGALR-UEl6nhOg_f0tM1vjbniYoQxcgXAAYYJLSSDz9QkpeMCj29YJ-q9wle54TROHhX3ra4o8Z23CZz3EEiIO0o8ywjmOZnjFKctJns4v0cTT5ytsY8f3hSlRqiR5eVmMfBJOdQJrq6n0L4kvQ9FBexQEvcGgAeVBVVT7K_YLf4EieY3EJ0tnI3MZ9wdpbhOcpO-XyzAtc0e77KGkNc8OrfwaAhvDvgjrtieiKoK1F9rbsY4FueznQ1xyuq5obktiDN8AIRCqsrf3zBCa8jxOeiDNHrm15rbfsDRE8cgfIaCJYscR4XcifqyUJZZ-gB81EcfFQ3XujBFmr1D5Ejnkc9W6x8K9WKqmbymZG0bzrUplF9G5_mOUqWEVI_mJdy-0GKabp93pWt1OUdTaM7AhFmcM2Y6MjUYksgDQjEw7ddTEx0fGtpYgfClIhdaG2x0kqgyBec3YnG6muHlRblhJQM9Fjf2xW0FnIS7os3jpFaSLjebGYzzHF6qV5ZeHec5AXeRxCTf1HPtrAFWb9hiKcQ4uWtY782JKpyQf22u0N7nVVFYvdF3oFCekTr-IHO2C5L--sc57IgVTSv5FON5rVFsUDinIm8cdIUkGfKjZ_WKcpxtIoDybNS6o2majNI8QD0NaMQd0S8J-D1QpoP9fQOkcIMD_QtnmGUe0FfoFC6PplxttJr7Cx5YUOZ_S0m8A11RHbMoyqd_HuVRHWtp_YqurdV87AzsBpw1UI8-Vt1amOSMMjaB9XdhjSzcTdIcxGfI9bbxBvt7DaPfaJLyu7oBypyvkmkAm1cG7B9fKHFFJNQE68thoe-nbaZTqZ54e8nbPFgendBBU25Pg0bMTM06wlk0UcugZ1ybOZDshXZa8q_puxryrcfPJhdIA4zDDDEdLMqnlRtHep7j4w_GcX9-qsLIoT1CipqS3IYgpzu_VU3S1eX65vrG6eUGmhONYTJMMEaIbrvBw-xrE59cBJXc5srwWaQM-1FqKierZhvyD1zFq8-5LaNG51mjZZBI0F6cpzoGwu82lpbM72OZjlga1Kkhyrthp53xLYVqNqQlQuWYnNgfM7YOuqIr8dlf88tR_Smdl3Ai67sgUygZxfjSidluN5n6S2X6tzV1LKLNXqSKaK4kdDe-MAUsS6HK_njiatzQvGYX0mG3RkX8-EV0x0x-mqNrx19s90S_C7nHc2rIy3sl2eMr8zptgvdM8chzy55gSAj3WIs3W1n-b2qL_V3D9Y7dzopnerWDIAI7XW37g0quobx8pRz9BBp4kCx2rqh4bhlz8NG9X263niy2x5H8ZwIqEpQ-LfFtmmEhxFfqnbaARX2Dy6qfOOyPM0gFSVItBxEYyrP7D0VXUFTrKAaKa3qXQfXPd06nMwvfVBb6X7gumTPJR1DMmVo7aPtBxmm83xRtDumocMxl4mDpLq9n86QiC8kXxD6hm0gg1Ucqih8CvF_i_ssB2jlTxn4Zcl7TvwX9dUQG_UcTfbaj_Sl2XZeOyZacLDXEGAXBTyXOotqt-oZiaRUnyNt3QP2rBwm4rQblszKTnl-hrOszjOB8us7UfgLlsLxnAT83QQ4V55ogrYv8vRHVREVD92jsCGiTNXKyd1R2Z3bP2OekwRl1pzOOPnGgCqzbaxM1ItgK1W1TXWMVnWxaTiahsEvgwHcHcGdMjkOoMBsMWtIowKpFpkFgSYxDo-n4UGNsDgANPgbyMNh-ZUREDYC-zHME3UTTf8tBEhE1fJNjALmM0b7wlDL4ONRyYD61NFI9tZ2GtKaXPM8Jg7uOFoLP6H6blCb_jSeuSkzjZJiNKy-LuOlZqUN0u_KLxOB8d78ciH3rcpOYxVMT2w3uRVWsaWqO8MASDzbxLjIMi-ys5AGvLB7OCzuTJ9YUWZwNJfcs2TbrzXWpykH7jmCamLU-InIwW06B2rANk56GmS3Qj0nNWE_nHOShjHIx_1whfkKycdQVahpmC_wCk_DGG5TPEMyaMIpfQSyNaL_MLYqKTnbzBdhPEOZgKeN2jQV_3VRoaiPMGcwTeRhfHgyGikmYfw9vA_jo-Hx_oujo_3D0eHo5cHB4W94cNwPH8J4f7gvr5MXJ4cv9k_2R8cv948PDo8e--E3JZ5usuzxP4AfaDo](https://www.mermaidchart.com/play?utm_source=mermaid_live_editor&utm_medium=toggle#pako:eNq1WUtv2zgQ_iuCkYO8dQykwF6EooCTZrcFknURp-ku4hxYibYJy6RByknTbv_7Dh-SKT5kedP6kMiab56cGQ7p74OcFXiQDfISCfGOoCVHmzmd0wQ-FG2w2KIcJ4wvxwLzR8zHmHPGV4gWJaHL5LtGyo-SkNxgsSur-_lAYFTiIiG0wnwhZWhKkt6OkpvhfPBg88rPK7ZOH1G5w1lyOzTo8XSt8ScOVlmRqr8ZkGv4pXpdczg8RGjyMPnCWImRTwdtFtEhVytM00WWLGgK9p2-NTrTmdQ2bH1zODdoC5Jt1lkM_8ON53SdQDA5zhkv3KAEglgHsEOiCoIv1ApdQG4d6ZZcV_JMZYgSFEoAiwwaOtnHHyipakHGziA5ZOrXHG8rwmiWXNaPHeFoiZ126Zz20AigI3VelATT6oJRivMul33cr7HkI2dbzKvng6a0gAFbNmKZJbOKQ5voG34K6UuK99BcxAqtcWcCBKAqqw6Kv2aP-AbnmDxCdnZKt5EHgl2WeInKCV_uNhCaeOR11cTbq99QtV3XiFCwVf4LhRpep4gvRR3w-4dhZ6X-CapnnkL5ORUsX-MqM3pn6psD2ZboGfOzLPmoHoLU13uq0w1FhXiVDl218q00K21a4x0jxcheF5dH7UJ4UlUoX6dI_cO81jtKCkyL_fdjxUpbLmmRPhGoOEtqyURPoY5YAj2LQFp9O8bFXNe71iaOYCyIOIbXVSu9BI5qxdmTiPYSN620Ki-lZKJn-tntB62Uk_RQvnGM1ELC53y3WGCOixv1ysE9cVIZ3EdQU31W311lK7I9Z4gXkOL1o-e_diRtakH97w6HjjprOtLxi3wEh4yItvEDXbBjlvbVEleyrzU8b-S3Fs9bh2OFxBUTVXRuElBnKoyftgWqcLrJEimzMepcc3U5pGQAfMso5B0R7wlE3WgLQSdPiBg3AtR3eIFRFSR9hkbpx2TB2Uavsbfkxoe6-jtc4DvqqexZR005hSfvXO23hyZvvSs30503fln0wEbZTFTWlmhxdE9cx00b9gwT0eYDX6I2OCxE_ew3WHyia8qe6AcqKr7LpQFdUiPokFzYXcykGZO1R4TWsZM3zicr3MT7gC8BZHCcNzz1-kROFHZWtmvH4U99Brfa2CIEcg05iZVerPBipfaTZ7JrxGF8AYGY7jbmm2qrY_0c0PEXo3jUfnVNhHBeQTPtKA5Lkbdxf9H7pG_L_cP9g9dFLZjXCGEozLEGpF9HyXMmG9MIgsTVyOZrcAVUTEcRmmngTBGaby-8xWvPt12TRq8xo2PGyJEeGmf6wSFv9t62XG_DnADsXUr1PKXDMAxO164BzQ7k1IJj2BIEXzG2TXvi9aQc3m7r6Ty2Uxu62ZADlBls12ZS6WUM1yc1fWhTm3YzsBzjjR5iTBZHGd0zDwwQ23qukv8CeXVFKJ7yWymwv2AzGt4yvSOmX5zJtWds9qeBP-Soo2X15IVD_oLxjT3Y9uF74SQUOCCvEFSk34xluN0q_6r6S_vds_POn07qoPo9AyhCR93ve6cNX2SynHCOnlPNnChR-zBE7g1OAmLUvk-3u0B1u5MoXhIBXQka_77ZxqZ3mO6laZMeUDg6-FD5xhc5KaEUJUl03KhiKi-fAx1dUQusqBpUNP2uR-gObx1e5dcxaK30KPFDcuKzTqGYSrQN8Y6SEtNltTLbHdPU8ZTLwkHS3OEvF0jEZ1KtCD1nO6hglYcqCw8B_7e6OzlAq3jKxK9b3kvy3_RXS2069Cw56b6blm67de256NDBX0uB2xTwUtosmoNqYCSSWkOBdG1P2ItqmIhJP5Ssyl51foHLsi0zh_Ybukz4G5bCi5wk_BMjXKlIxKjdizz_WV1E5UP_LIxklG1aPbl7Jvtz-x3mFclR6czpjJNvDLhK18fGRb0IrlHNtqlu0JpdbD44mw-S305P4el3eFIuZwk0mD2yBTozoFZmGgbNYt0bzwevW4zm7s-Sb4HH4_rnMgDsBA4jrMuVFkz_NQokUG35NsLQQs7oWFhmWXICJlnUkDka5B5t5wPa0mtfxWTJE0dbEWZ88y_wtKY_jbMPZbZTUo2mtdfFOKLETdfOu0Tq0Adss_DNTwrKT2sV7EjsD7kNyhyp2sGwCBLnupiZKguCvYW06Mbv8dg82TFxssySaC95YMnUkqgw7S-0wvTAFVRMUOjGKIyN3QNF0NZNT0R3JzV0U7PvLHZ6Wx671330MHbaFxn4DbMvU_vXxh5GxVelD1_rpz_FNfjxH1GDLMQ))

## Sequence Diagram

```mermaid
sequenceDiagram
    box client
        actor Alice
        participant GameClient1
    end

    box server
        participant Player1
        participant GameServer
        participant Player2
    end
    
    box client
        participant GameClient2
        actor Bob
    end

    activate GameServer
        GameServer ->> Player2: sendAwaitMove()
    
        Player2 ->> GameClient2: "AWAIT MOVE"
        GameClient2 ->> Bob: print("Enemy's turn.")
    
        GameServer ->> Player1: getMove()
        activate Player1
            Player1 ->> GameClient1 : "SEND MOVE"

            activate GameClient1
                GameClient1 ->> Alice: promptMove()
                activate Alice
                    Alice -->> GameClient1: Move
                deactivate Alice
                GameClient1 -->> Player1: Move
                Player1 -->> GameServer: Move
                GameServer ->> Player2: sendBoardUpdate(Move)
                activate Player2
                    Player2 ->> GameClient2: "UPDATE ..<board>.. END"
                    GameClient2 ->> Bob : print updated board
                    Player2 -->> GameServer: boolean isHit
                deactivate Player2

                GameClient1 -->> Player1: recieveMoveResult()
                GameServer ->> Player1: sendResponse(isHit)
                
            Player1 ->> GameClient1: "HIT" | "MISS"
        deactivate Player1

                GameClient1 ->> Alice: print result
            deactivate GameClient1

        alt Player2 has lost
            GameServer ->> Player1: sendWin()
            Player1 ->> GameClient1: "WIN"        
            GameClient1 ->> Alice: print win msg

            GameServer ->> Player2: sendDefeat()
            Player2 ->> GameClient2: "DEFEAT"
            GameClient2 ->> Bob: print defeat msg
            
            GameServer ->> Player1: disconnectPlayers()
            GameServer ->> Player2: disconnectPlayers()

        end
    deactivate GameServer        
```

### Testen
Der andere Spieler muss lange warten.

### Zusatzaufgabe (noch nicht fertig)
1. ggf. Timeout, gegenspieler kann keinen input eingeben
2. der Gegenspieler muss immer so lange warten, bis der Spieler mit seinem Zug fertig ist -> zieht das Spiel in die Länge
3. gleichzeitig könnten gleichzeitig ihren Zug platzieren, ggf. auch spannender, wenn die Züge gleichzeitig ausgeführt werden
4. 
