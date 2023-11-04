//
//  SoundModel.swift
//  iosApp
//
//  Created by Ashish Ekka on 04/11/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import common
import AVKit
import Foundation
import KMPNativeCoroutinesAsync

@MainActor
class SoundModel : ObservableObject {
    @Published var sound: Bool
    var isPlayingSound = false
    let chip8: Chip8
    init(chip8: Chip8) {
        self.sound = false
        self.chip8 = chip8
        Task { await startObservingSound() }
    }
    
    private func startObservingSound() async {
        do {
            let stream = asyncSequence(for: Chip8NativeKt.getSoundFlow(chip8))
            for try await data in stream {
                if (data.boolValue && !isPlayingSound) {
                    AudioServicesPlaySystemSoundWithCompletion(1306) {
                        print("Beep")
                        self.isPlayingSound = false
                    }
                    isPlayingSound = true
                }
            }
        } catch {
            print("failed with error")
        }
    }
}
