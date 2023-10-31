import SwiftUI
import common

struct ContentView: View {
    
    @StateObject var viewModel = MainViewModel(chip8: Chip8Impl())
    
    @State private var showSettings = false
    
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
                        showSettings = true
                    } label: {
                        Image(systemName: "gearshape")
                    }
                }
            }
            .sheet(isPresented: $showSettings) {
                SettingsView { data in
                    showSettings = false
                    dump(data)
                }
            }
        }
	}
}

#Preview {
    ContentView(viewModel: MainViewModel(chip8: Chip8Impl()))
}
