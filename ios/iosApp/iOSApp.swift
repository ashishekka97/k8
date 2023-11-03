import SwiftUI
import common

@main
struct iOSApp: App {
	var body: some Scene {
		WindowGroup {
			ContentView(k8Settings: K8Settings(), chip8: Chip8Impl())
		}
	}
}
