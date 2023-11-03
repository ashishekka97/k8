import SwiftUI
import common

struct ContentView: View {
    @StateObject var viewModel: MainViewModel
    @State private var showSettings = false
    @State private var theme = ThemeColor.default_
    var k8Settings: K8Settings
    var chip8: Chip8
    
    init(k8Settings: K8Settings, chip8: Chip8) {
        self.k8Settings = k8Settings
        self.chip8 = chip8
        self._viewModel = StateObject(wrappedValue: MainViewModel(chip8: chip8, settings: k8Settings))
        self.showSettings = false
    }
    
    
    var body: some View {
        NavigationView {
            VStack(alignment: .leading) {
                Screen(viewModel: viewModel, theme: self.$theme)
                    .task {
                        viewModel.loadAndStart()
                        viewModel.startObservation()
                    }
                Spacer()
                KeyPad { key in
                    viewModel.onKeyDown(key: key)
                } onKeyUp: { key in
                    viewModel.onKeyUp(key: key)
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
                SettingsView(k8Settings: K8Settings(), successFullyChanged: { data in
                    let speed = Float((data.first(where: { $0.key == .speed })?.optionVal?.name ?? "1.0")) ?? 1.0
                    print(speed)
                    chip8.emulationSpeedFactor(factor: speed)
                    
                    let theme = ThemeColor.companion.getThemeFromKey(key: data.first(where: { $0.key == .theme })?.optionVal?.name ?? "default")
                    print(Color(hex: theme.background))
                    self.theme = theme
                    
                    showSettings = false
                })
            }
        }
    }
}

#Preview {
    ContentView(k8Settings: K8Settings(), chip8: Chip8Impl())
}
