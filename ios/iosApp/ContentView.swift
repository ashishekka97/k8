import SwiftUI
import common

struct ContentView: View {
    
    @StateObject var mainViewModel: MainViewModel
    @StateObject var settingsViewModel: SettingsViewModel
    @State private var showSettings = false
    var k8Settings: K8Settings
    var chip8: Chip8
    
    init(k8Settings: K8Settings, chip8: Chip8) {
        self.k8Settings = k8Settings
        self.chip8 = chip8
        self._mainViewModel = StateObject(wrappedValue: MainViewModel(chip8: chip8))
        self._settingsViewModel = StateObject(wrappedValue: SettingsViewModel(settings: k8Settings))
        self.showSettings = false
    }
    
    
    var body: some View {
        NavigationView {
            VStack(alignment: .leading) {
                Screen(viewModel: mainViewModel, settingsViewModel: settingsViewModel)
                Spacer()
                KeyPad { key in
                    mainViewModel.onKeyDown(key: key)
                } onKeyUp: { key in
                    mainViewModel.onKeyUp(key: key)
                }
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
                SettingsView(viewModel: self.settingsViewModel, successFullyChanged: {
                    let selectedSpeedKey = settingsViewModel.settings.first(where: { $0.key == .speed })?.optionVal?.name ?? "1.0"
                    let speed = EmulatorSpeed.companion.getSpeedFromKey(key: Float(selectedSpeedKey) ?? 1.0)
                    mainViewModel.changeEmualatorSpeed(speed: speed)
                    showSettings = false
                })
            }.task {
                mainViewModel.loadAndStart()
                await settingsViewModel.observeSettings()
                await mainViewModel.startObservation()
            }
        }
    }
}

#Preview {
    ContentView(k8Settings: K8Settings(), chip8: Chip8Impl())
}
