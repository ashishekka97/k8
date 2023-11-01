//
//  AsyncCombineLatest4Sequence.swift
//  iosApp
//
//  Created by Ashish Ekka on 01/11/23.
//  Copyright © 2023 orgName. All rights reserved.
//

/// Creates an asynchronous sequence that combines the latest values from three `AsyncSequence` types
/// by emitting a tuple of the values. ``combineLatest(_:_:_:_:)`` only emits a value whenever any of the base `AsyncSequence`s
/// emit a value (so long as each of the bases have emitted at least one value).
/// This implementation is a rewrite of ``combineLatest(_:_:_:)`` from the `swift-async-algorithms` package.
///
/// Finishes:
/// ``combineLatest(_:_:_:_:)`` finishes when one of the bases finishes before emitting any value or
/// when all bases finished.
///
/// Throws:
/// ``combineLatest(_:_:_:_:)`` throws when one of the bases throws. If one of the bases threw any buffered and not yet consumed
/// values will be dropped.
public func combineLatest<
    Base1: AsyncSequence,
    Base2: AsyncSequence,
    Base3: AsyncSequence,
    Base4: AsyncSequence
>(_ base1: Base1, _ base2: Base2, _ base3: Base3, _ base4: Base4) -> AsyncCombineLatest4Sequence<Base1, Base2, Base3, Base4> where
Base1: Sendable,
Base1.Element: Sendable,
Base2: Sendable,
Base2.Element: Sendable,
Base3: Sendable,
Base3.Element: Sendable,
Base4: Sendable,
Base4.Element: Sendable {
    AsyncCombineLatest4Sequence(base1, base2, base3, base4)
}

/// An `AsyncSequence` that combines the latest values produced from three asynchronous sequences into an asynchronous sequence of tuples.
public struct AsyncCombineLatest4Sequence<
    Base1: AsyncSequence,
    Base2: AsyncSequence,
    Base3: AsyncSequence,
    Base4: AsyncSequence
>: AsyncSequence, Sendable where
Base1: Sendable,
Base1.Element: Sendable,
Base2: Sendable,
Base2.Element: Sendable,
Base3: Sendable,
Base3.Element: Sendable,
Base4: Sendable,
Base4.Element: Sendable {
    public typealias Element = (Base1.Element, Base2.Element, Base3.Element, Base4.Element)
    public typealias AsyncIterator = Iterator
    
    let base1: Base1
    let base2: Base2
    let base3: Base3
    let base4: Base4
    
    init(_ base1: Base1, _ base2: Base2, _ base3: Base3, _ base4: Base4) {
        self.base1 = base1
        self.base2 = base2
        self.base3 = base3
        self.base4 = base4
    }
    
    public func makeAsyncIterator() -> AsyncIterator {
        Iterator(storage: .init(self.base1, self.base2, self.base3, self.base4))
    }
    
    public struct Iterator: AsyncIteratorProtocol {
        final class InternalClass {
            private let storage: CombineLatestStorage<Base1, Base2, Base3, Base4>
            
            fileprivate init(storage: CombineLatestStorage<Base1, Base2, Base3, Base4>) {
                self.storage = storage
            }
            
            deinit {
                self.storage.iteratorDeinitialized()
            }
            
            func next() async rethrows -> Element? {
                guard let element = try await self.storage.next() else {
                    return nil
                }
                
                // This force unwrap is safe since there must be a third element.
                return (element.0, element.1, element.2, element.3!)
            }
        }
        
        let internalClass: InternalClass
        
        fileprivate init(storage: CombineLatestStorage<Base1, Base2, Base3, Base4>) {
            self.internalClass = InternalClass(storage: storage)
        }
        
        public mutating func next() async rethrows -> Element? {
            try await self.internalClass.next()
        }
    }
}
