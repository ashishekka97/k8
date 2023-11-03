//
//  MainViewModel.swift
//  iosApp
//
//  Created by Ashish Ekka on 23/10/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import common
import KMPNativeCoroutinesAsync
import SwiftUI

@MainActor
class MainViewModel : ObservableObject {
    @Published var vRam = [[Bool]](repeating: [Bool](repeating: Bool.random(), count: 64), count: 32)
    @Published var sound = false
    @Published var speed = EmulatorSpeed.full
    @Published var theme = ThemeColor.default_
    
    let chip8: Chip8
    let k8Settings: K8Settings
    init(chip8: Chip8, settings: K8Settings) {
        self.chip8 = chip8
        self.k8Settings = settings
    }
    
    func startObservation() {
        _ = Task {
            await withTaskGroup(of: Void.self) { taskGroup in
                taskGroup.addTask { await self.startObservingSpeed() }
                taskGroup.addTask { await self.startObservingVRam() }
            }
        }
    }
    
    private func startObservingVRam() async {
        do {
            let stream = asyncSequence(for: Chip8NativeKt.getVideoMemoryFlow(chip8))
            for try await data in stream {
                self.vRam = mapToBool(vRam: data)
            }
        } catch {
            print("failed with error")
        }
    }
    
    private func startObservingSound() async {
        do {
            let soundSequence = asyncSequence(for: k8Settings.getBooleanFlowSetting(key: SettingUiModel.Key.sound.rawValue))
            for try await sound in soundSequence {
                print(sound)
                self.sound = sound.boolValue
            }
        } catch {
            print("Error in observing sound")
        }
    }
    
    private func startObservingTheme() async {
        print("start observing speed")
        do {
            let themeSequence = asyncSequence(for: k8Settings.getIntFlowSetting(key: SettingUiModel.Key.theme.rawValue))
            for try await theme in themeSequence {
                print(theme)
                self.theme = ThemeColor.companion.getThemeFromIndex(index: theme.int32Value)
            }
        } catch {
            print("Error in observing theme")
        }
    }
    
    private func startObservingSpeed() async {
        print("start observing speed")
        do {
            let speedSequence = asyncSequence(for: k8Settings.getIntFlowSetting(key: SettingUiModel.Key.speed.rawValue))
            for try await speed in speedSequence {
                print(speed)
                self.speed = EmulatorSpeed.companion.getSpeedFromIndex(index: speed.int32Value)
            }
        } catch {
            print("Error in observing speed")
        }
    }
    
    func mapToBool(vRam: KotlinArray<KotlinBooleanArray>) -> [[Bool]] {
        var result = [[Bool]]()
        let iterator = vRam.iterator()
        var rowIndex = 0
        while (iterator.hasNext()) {
            var swiftRow = [Bool]()
            let row = iterator.next() as! KotlinBooleanArray
            let rowIterator = row.iterator()
            var colIndex = 0
            while(rowIterator.hasNext()) {
                swiftRow.insert(rowIterator.nextBoolean(), at: colIndex)
                colIndex += 1
            }
            result.insert(swiftRow, at: rowIndex)
            rowIndex += 1
        }
        
        return result
    }
    
    func loadAndStart() {
        if let bytes: [UInt8] = getFile(forResource: "invader", withExtension: "ch8") {
            let intArray : [Int8] = bytes.map { Int8(bitPattern: $0) }
            let kotlinByteArray: KotlinByteArray = KotlinByteArray.init(size: Int32(bytes.count))
            for (index, element) in intArray.enumerated() {
                kotlinByteArray.set(index: Int32(index), value: element)
            }
            chip8.loadRom(romBytes: kotlinByteArray)
            chip8.start()
        }
    }
    
    func getFile(forResource resource: String, withExtension fileExt: String?) -> [UInt8]? {
        // See if the file exists.
        guard let fileUrl: URL = Bundle.main.url(forResource: resource, withExtension: fileExt) else {
            return nil
        }
        
        do {
            // Get the raw data from the file.
            let rawData: Data = try Data(contentsOf: fileUrl)

            // Return the raw data as an array of bytes.
            return [UInt8](rawData)
        } catch {
            // Couldn't read the file.
            return nil
        }
    }
    
    func onKeyDown(key: Int) {
        chip8.onKey(key: Int32(key), type: KeyEventType.down)
    }
    
    func onKeyUp(key: Int) {
        chip8.onKey(key: Int32(key), type: KeyEventType.up)
    }

}
