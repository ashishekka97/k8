import SwiftUI
import common

struct ContentView: View {
    
    @StateObject var mainViewModel: MainViewModel
    @StateObject var settingsViewModel: SettingsViewModel
    @State private var showSettings = false
    @State private var showFilePicker = false
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
                .task {
                    let selectedSpeedKey = settingsViewModel.settings.first(where: { $0.key == .speed })?.optionVal?.name ?? "1.0"
                    let speed = EmulatorSpeed.companion.getSpeedFromKey(key: Float(selectedSpeedKey) ?? 1.0)
                    mainViewModel.changeEmualatorSpeed(speed: speed)
                    let sound = settingsViewModel.settings.first(where: { $0.key == .sound })?.boolVal ?? false
                    mainViewModel.toggleSound(isEnabled: sound)
                    mainViewModel.loadAndStart()
                    await settingsViewModel.observeSettings()
                }
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Text("K8 (Kate)")
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showFilePicker = true
                    } label: {
                        Image(systemName: "filemenu.and.selection")
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        mainViewModel.loadAndStart()
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
            .fileImporter(isPresented: $showFilePicker, allowedContentTypes: [.data]) { result in
                do {
                    let url = try result.get()
                    mainViewModel.loadAndStart(url: url)
                } catch {
                    print("Error in opening file")
                }
            }
            .sheet(isPresented: $showSettings) {
                SettingsView(viewModel: self.settingsViewModel, successFullyChanged: {
                    let selectedSpeedKey = settingsViewModel.settings.first(where: { $0.key == .speed })?.optionVal?.name ?? "1.0"
                    let speed = EmulatorSpeed.companion.getSpeedFromKey(key: Float(selectedSpeedKey) ?? 1.0)
                    mainViewModel.changeEmualatorSpeed(speed: speed)
                    
                    let sound = settingsViewModel.settings.first(where: { $0.key == .sound })?.boolVal ?? false
                    mainViewModel.toggleSound(isEnabled: sound)
                    showSettings = false
                })
            }
        }
    }
}

#Preview {
    ContentView(k8Settings: K8Settings(), chip8: Chip8Impl())
}
