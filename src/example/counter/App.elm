--https://gist.github.com/CliffordAnderson/972907dc8c98b954290723bc68de5fd6

module Main exposing (..)

import Html exposing (br, button, div, text)
import Html.Events exposing (onClick)
import Html.App exposing (beginnerProgram)


main : Program Never
main =
    beginnerProgram { model = model, view = view, update = update }


type Msg
    = Increment
    | Decrement


model : Int
model =
    0


view : Int -> Html.Html Msg
view model =
    div []
        [ button [ onClick Increment ] [ text "+" ]
        , br [] []
        , text (toString model)
        , br [] []
        , button [ onClick Decrement ] [ text "-" ]
        ]


update : Msg -> Int -> Int
update msg model =
    case msg of
        Increment ->
            model + 1

        Decrement ->
            model - 1