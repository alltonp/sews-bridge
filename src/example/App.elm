module Main exposing (..)

import Codec exposing (..)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Json.Decode
import Json.Encode
import WebSocket


main =
    Html.program
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }



-- MODEL


type alias Model =
    { result : Result String ServerModel
    }


init : ( Model, Cmd Msg )
init =
    ( Model (Err "Hello"), Cmd.none )



-- UPDATE


type Msg
    = FromServer String



--TODO: make ws url a flag


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        FromServer str ->
            let
                model_ =
                    case Json.Decode.decodeString decodeFromServer str of
                        Ok msg ->
                            case msg of
                                FromServerWsModelUpdated m ->
                                    { model | result = Ok m.serverModel }

                        Err x ->
                            { model | result = Err x }
            in
            ( model_, Cmd.none )


sendToServer : ToServer -> Cmd msg
sendToServer serverMsg =
    WebSocket.send "ws://localhost:9000/ws/test" (Json.Encode.encode 0 (encodeToServer serverMsg))



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    WebSocket.listen "ws://localhost:9000/ws/test" FromServer



-- VIEW


view : Model -> Html Msg
view model =
    div []
        [ div [] [ text ("Last result: " ++ toString model.result) ]
        ]