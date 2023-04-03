
import SwiftUI
import AnnotationsClient

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel

    var body: some View {
//        VStack {
//            Text("🚀 Total Rockets Launched: " + String(viewModel.rocketsLaunched?.size ?? 0))
//            Text("First Rocket Mission: " + String(viewModel.rocketsLaunched?.get(index: 0)?.mission_name ?? "loading"))
//            Text("First Rocket name: " + String(viewModel.rocketsLaunched?.get(index: 0)?.rocket?.name ?? "loading"))
//        }
        VStack {
            Text("🚀 Total Rockets Launched: " + String(viewModel.rocketsLaunched.count))
            Text("First Rocket Mission: " + String(viewModel.rocketsLaunched.count > 0 ? viewModel.rocketsLaunched[0].mission_name ?? "" : "loading"))
            Text("First Rocket name: " + String(viewModel.rocketsLaunched.count > 0 ? viewModel.rocketsLaunched[0].rocket?.name ?? "" : "loading"))
        }
        .padding()
    }

}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(viewModel: .init())
    }
}

extension ContentView {

    class ViewModel: ObservableObject {
//        @Published var rocketsLaunched: KotlinArray<RocketLaunch>?
        @Published var rocketsLaunched: [RocketLaunch] = []
        let annotationsClientManager = AnnotationsClientManager.Builder(databaseDriverFactory: DatabaseDriverFactory()).build()

        init() {
            loadRockets()
        }

        func loadRockets() {
            annotationsClientManager.fetchLaunchRockets(forceReload: true)
//                .onSuccess  { result in
//                    guard let rocketsResult = result else { return }
//                    print("Total Rockect Missions:, \(rocketsResult.size)!")
//                    print("First Rockect Mission:, \(rocketsResult.get(index: 0)?.mission_name ?? "")!")
//                }.onSuccessUI  { [self] result in
//                    guard let rocketsResult = result else { return }
//                    rocketsLaunched = rocketsResult
//                }

            annotationsClientManager.getRocketLaunchesListFlow(param: "").onSuccessUI { (flow: CommonFlow<RocketLaunchList>?) in
                flow?.watch { [self] (result: RocketLaunchList?, v2: ClientException?) in
                    guard let list = result?.list else { return }
                    rocketsLaunched = list
                }
            }
        }
    }

}