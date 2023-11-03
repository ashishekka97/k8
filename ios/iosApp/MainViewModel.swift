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
import AVKit

@MainActor
class MainViewModel : ObservableObject {
    @Published var vRam = [[Bool]](repeating: [Bool](repeating: Bool.random(), count: 64), count: 32)
    var currentRom: URL? = nil
    
    let chip8: Chip8
    init(chip8: Chip8) {
        self.chip8 = chip8
        currentRom = getUrlFromResource(forResource: "invader", withExtension: "ch8")
        Task { await startObservation() }
    }
    
    func startObservation() async {
        await startObservingVRam()
        await startObservingSound()
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
            let stream = asyncSequence(for: Chip8NativeKt.getSoundFlow(chip8))
            for try await data in stream {
                print(data)
                if (data.boolValue) {
                    AudioServicesPlaySystemSound(1026)
                }
            }
        } catch {
            print("failed with error")
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
    
    func loadAndStart(url: URL? = nil) {
        let fileUrl = url ?? currentRom
        if let bytes: [UInt8] = getFile(url: fileUrl) {
            let intArray : [Int8] = bytes.map { Int8(bitPattern: $0) }
            let kotlinByteArray: KotlinByteArray = KotlinByteArray.init(size: Int32(bytes.count))
            for (index, element) in intArray.enumerated() {
                kotlinByteArray.set(index: Int32(index), value: element)
            }
            chip8.loadRom(romBytes: kotlinByteArray)
            currentRom = fileUrl
            chip8.start()
        }
    }
    
    private func getUrlFromResource(forResource resource: String, withExtension fileExt: String?) -> URL? {
        // See if the file exists.
        guard let fileUrl: URL = Bundle.main.url(forResource: resource, withExtension: fileExt) else {
            return nil
        }
        
        return fileUrl
    }
    
    func getFile(url: URL?) -> [UInt8]? {
        guard let fileUrl = url else {
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

    func changeEmualatorSpeed(speed: EmulatorSpeed) {
        chip8.emulationSpeedFactor(factor: speed.speedFactor)
    }
    
    func toggleSound(isEnabled: Bool) {
        chip8.toggleSound(isEnabled: isEnabled)
    }
}
