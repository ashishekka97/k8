//
//  ScreenState.swift
//  iosApp
//
//  Created by Ashish Ekka on 24/10/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import SwiftUI


struct ScreenState : Equatable, Hashable {
    var state: [[Bool]]
    var foregroundColor: Color
    var backgroundColor: Color
}
