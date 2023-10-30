import SwiftUI
import common

struct ContentView: View {
    @StateObject var viewModel = MainViewModel(chip8: Chip8Impl())
	var body: some View {
        NavigationView {
            VStack(alignment: .leading) {
                Screen(viewModel: viewModel)
                .task {
                    viewModel.loadAndStart()
                    await viewModel.startObservingVRam()
                }
                Spacer()
                KeyPad()
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Text("K8 (Kate)")
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        print("Load")
                    } label: {
                        Image(systemName: "filemenu.and.selection")
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        print("Reset")
                    } label: {
                        Image(systemName: "arrow.circlepath")
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        print("Settings")
                    } label: {
                        Image(systemName: "gearshape")
                    }
                }
            }
        }
	}
}
