# CardParty

**Play it at http://cardparty.herokuapp.com**

A group card game as a webapp using websockets.

Website and cli tool written in kotlin multiplatform with: 
- ktor with websockets
- kotlin-react
- kotlinx-serialization

## Structure

1. Server (in `jvmMain/server`)
2. Cli client (in `jvmMain/cli`)
3. JS webapp (in `jsMain/web`)
  - `App.kt` is the main file, which instantiates all the components and own the important state
  - `components` contains react components used by the `App`
  - `receive` handles unidentified requests
4. `commonMain`, contains platform independent code
  - `request`, contains all serialized data classes that can be sent over the websocket
  - `resources`, contains static html files

To deploy to heroku push to the heroku branch.