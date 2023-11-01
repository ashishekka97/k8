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
    
    let chip8: Chip8
    init(chip8: Chip8) {
        self.chip8 = Chip8Impl()
    }
    
    func startObservingVRam() async {
        do {
            let stream = asyncSequence(for: Chip8NativeKt.getVideoMemoryFlow(chip8))
            for try await data in stream {
                self.vRam = mapToBool(vRam: data)
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

}
