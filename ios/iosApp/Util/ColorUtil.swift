//
//  ColorUtil.swift
//  iosApp
//
//  Created by Ashish Ekka on 03/11/23.
//  Copyright © 2023 orgName. All rights reserved.
//
import SwiftUI

extension Color {
    init(hex: Int64) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 0xff,
            green: Double((hex >> 08) & 0xff) / 0xff,
            blue: Double((hex >> 00) & 0xff) / 0xff,
            opacity: Double((hex >> 24) & 0xff) / 0xff
        )
    }
}
