//
//  CombineLatestStorage.swift
//  iosApp
//
//  Created by Ashish Ekka on 01/11/23.
//  Copyright © 2023 orgName. All rights reserved.
//

final class CombineLatestStorage<
    Base1: AsyncSequence,
    Base2: AsyncSequence,
    Base3: AsyncSequence,
    Base4: AsyncSequence
>: Sendable where
Base1: Sendable,
Base1.Element: Sendable,
Base2: Sendable,
Base2.Element: Sendable,
Base3: Sendable,
Base3.Element: Sendable,
Base4: Sendable,
Base4.Element: Sendable {
    typealias StateMachine = CombineLatestStateMachine<Base1, Base2, Base3, Base4>
    
    private let stateMachine: ManagedCriticalState<StateMachine>
    
    init(_ base1: Base1, _ base2: Base2, _ base3: Base3, _ base4: Base4) {
        self.stateMachine = .init(.init(base1: base1, base2: base2, base3: base3, base4: base4))
    }
    
    func iteratorDeinitialized() {
        let action = self.stateMachine.withCriticalRegion { $0.iteratorDeinitialized() }
        
        switch action {
        case .cancelTaskAndUpstreamContinuations(
            let task,
            let upstreamContinuation
        ):
            upstreamContinuation.forEach { $0.resume(throwing: CancellationError()) }
            
            task.cancel()
            
        case .none:
            break
        }
    }
    
    func next() async rethrows -> (Base1.Element, Base2.Element, Base3.Element, Base4.Element?)? {
        try await withTaskCancellationHandler {
            let result = await withUnsafeContinuation { continuation in
                self.stateMachine.withCriticalRegion { stateMachine in
                    let action = stateMachine.next(for: continuation)
                    switch action {
                    case .startTask(let base1, let base2, let base3, let base4):
                        // first iteration, we start one child task per base to iterate over them
                        self.startTask(
                            stateMachine: &stateMachine,
                            base1: base1,
                            base2: base2,
                            base3: base3,
                            base4: base4,
                            downStreamContinuation: continuation
                        )
                        
                    case .resumeContinuation(let downstreamContinuation, let result):
                        downstreamContinuation.resume(returning: result)
                        
                    case .resumeUpstreamContinuations(let upstreamContinuations):
                        // bases can be iterated over for 1 iteration so their next value can be retrieved
                        upstreamContinuations.forEach { $0.resume() }
                        
                    case .resumeDownstreamContinuationWithNil(let continuation):
                        // the async sequence is already finished, immediately resuming
                        continuation.resume(returning: .success(nil))
                    }
                }
            }
            
            return try result._rethrowGet()
            
        } onCancel: {
            self.stateMachine.withCriticalRegion { stateMachine in
                let action = stateMachine.cancelled()
                
                switch action {
                case .resumeDownstreamContinuationWithNilAndCancelTaskAndUpstreamContinuations(
                    let downstreamContinuation,
                    let task,
                    let upstreamContinuations
                ):
                    upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                    task.cancel()
                    
                    downstreamContinuation.resume(returning: .success(nil))
                    
                case .cancelTaskAndUpstreamContinuations(let task, let upstreamContinuations):
                    upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                    task.cancel()
                    
                case .none:
                    break
                }
            }
        }
    }
    
    private func startTask(
        stateMachine: inout StateMachine,
        base1: Base1,
        base2: Base2,
        base3: Base3,
        base4: Base4?,
        downStreamContinuation: StateMachine.DownstreamContinuation
    ) {
        // This creates a new `Task` that is iterating the upstream
        // sequences. We must store it to cancel it at the right times.
        let task = Task {
            await withThrowingTaskGroup(of: Void.self) { group in
                // For each upstream sequence we are adding a child task that
                // is consuming the upstream sequence
                group.addTask {
                    var base1Iterator = base1.makeAsyncIterator()
                    
                    loop: while true {
                        // We are creating a continuation before requesting the next
                        // element from upstream. This continuation is only resumed
                        // if the downstream consumer called `next` to signal his demand.
                        try await withUnsafeThrowingContinuation { continuation in
                            self.stateMachine.withCriticalRegion { stateMachine in
                                let action = stateMachine.childTaskSuspended(baseIndex: 0, continuation: continuation)
                                
                                switch action {
                                case .resumeContinuation(let upstreamContinuation):
                                    upstreamContinuation.resume()
                                    
                                case .resumeContinuationWithError(let upstreamContinuation, let error):
                                    upstreamContinuation.resume(throwing: error)
                                    
                                case .none:
                                    break
                                }
                            }
                        }
                        
                        if let element1 = try await base1Iterator.next() {
                            self.stateMachine.withCriticalRegion { stateMachine in
                                let action = stateMachine.elementProduced((element1, nil, nil, nil))
                                
                                switch action {
                                case .resumeContinuation(let downstreamContinuation, let result):
                                    downstreamContinuation.resume(returning: result)
                                    
                                case .none:
                                    break
                                }
                            }
                        } else {
                            let action = self.stateMachine.withCriticalRegion { stateMachine in
                                stateMachine.upstreamFinished(baseIndex: 0)
                            }
                            
                            switch action {
                            case .resumeContinuationWithNilAndCancelTaskAndUpstreamContinuations(
                                let downstreamContinuation,
                                let task,
                                let upstreamContinuations
                            ):
                                
                                upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                                task.cancel()
                                
                                downstreamContinuation.resume(returning: .success(nil))
                                break loop
                                
                            case .cancelTaskAndUpstreamContinuations(let task, let upstreamContinuations):
                                upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                                task.cancel()
                                
                                break loop
                                
                            case .none:
                                break loop
                            }
                        }
                    }
                }
                
                group.addTask {
                    var base1Iterator = base2.makeAsyncIterator()
                    
                    loop: while true {
                        // We are creating a continuation before requesting the next
                        // element from upstream. This continuation is only resumed
                        // if the downstream consumer called `next` to signal his demand.
                        try await withUnsafeThrowingContinuation { continuation in
                            self.stateMachine.withCriticalRegion { stateMachine in
                                let action = stateMachine.childTaskSuspended(baseIndex: 1, continuation: continuation)
                                
                                switch action {
                                case .resumeContinuation(let upstreamContinuation):
                                    upstreamContinuation.resume()
                                    
                                case .resumeContinuationWithError(let upstreamContinuation, let error):
                                    upstreamContinuation.resume(throwing: error)
                                    
                                case .none:
                                    break
                                }
                            }
                        }
                        
                        if let element2 = try await base1Iterator.next() {
                            self.stateMachine.withCriticalRegion { stateMachine in
                                let action = stateMachine.elementProduced((nil, element2, nil, nil))
                                
                                switch action {
                                case .resumeContinuation(let downstreamContinuation, let result):
                                    downstreamContinuation.resume(returning: result)
                                    
                                case .none:
                                    break
                                }
                            }
                        } else {
                            let action = self.stateMachine.withCriticalRegion { stateMachine in
                                stateMachine.upstreamFinished(baseIndex: 1)
                            }
                            
                            switch action {
                            case .resumeContinuationWithNilAndCancelTaskAndUpstreamContinuations(
                                let downstreamContinuation,
                                let task,
                                let upstreamContinuations
                            ):
                                
                                upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                                task.cancel()
                                
                                downstreamContinuation.resume(returning: .success(nil))
                                break loop
                                
                            case .cancelTaskAndUpstreamContinuations(let task, let upstreamContinuations):
                                upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                                task.cancel()
                                
                                break loop
                                
                            case .none:
                                break loop
                            }
                        }
                    }
                }
                
                group.addTask {
                    var base1Iterator = base3.makeAsyncIterator()
                    
                    loop: while true {
                        // We are creating a continuation before requesting the next
                        // element from upstream. This continuation is only resumed
                        // if the downstream consumer called `next` to signal his demand.
                        try await withUnsafeThrowingContinuation { continuation in
                            self.stateMachine.withCriticalRegion { stateMachine in
                                let action = stateMachine.childTaskSuspended(baseIndex: 2, continuation: continuation)
                                
                                switch action {
                                case .resumeContinuation(let upstreamContinuation):
                                    upstreamContinuation.resume()
                                    
                                case .resumeContinuationWithError(let upstreamContinuation, let error):
                                    upstreamContinuation.resume(throwing: error)
                                    
                                case .none:
                                    break
                                }
                            }
                        }
                        
                        if let element3 = try await base1Iterator.next() {
                            self.stateMachine.withCriticalRegion { stateMachine in
                                let action = stateMachine.elementProduced((nil, nil,element3, nil))
                                
                                switch action {
                                case .resumeContinuation(let downstreamContinuation, let result):
                                    downstreamContinuation.resume(returning: result)
                                    
                                case .none:
                                    break
                                }
                            }
                        } else {
                            let action = self.stateMachine.withCriticalRegion { stateMachine in
                                stateMachine.upstreamFinished(baseIndex: 2)
                            }
                            
                            switch action {
                            case .resumeContinuationWithNilAndCancelTaskAndUpstreamContinuations(
                                let downstreamContinuation,
                                let task,
                                let upstreamContinuations
                            ):
                                
                                upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                                task.cancel()
                                
                                downstreamContinuation.resume(returning: .success(nil))
                                break loop
                                
                            case .cancelTaskAndUpstreamContinuations(let task, let upstreamContinuations):
                                upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                                task.cancel()
                                
                                break loop
                                
                            case .none:
                                break loop
                            }
                        }
                    }
                }
                
                if let base4 = base4 {
                    group.addTask {
                        var base1Iterator = base4.makeAsyncIterator()
                        
                        loop: while true {
                            // We are creating a continuation before requesting the next
                            // element from upstream. This continuation is only resumed
                            // if the downstream consumer called `next` to signal his demand.
                            try await withUnsafeThrowingContinuation { continuation in
                                self.stateMachine.withCriticalRegion { stateMachine in
                                    let action = stateMachine.childTaskSuspended(baseIndex: 3, continuation: continuation)
                                    
                                    switch action {
                                    case .resumeContinuation(let upstreamContinuation):
                                        upstreamContinuation.resume()
                                        
                                    case .resumeContinuationWithError(let upstreamContinuation, let error):
                                        upstreamContinuation.resume(throwing: error)
                                        
                                    case .none:
                                        break
                                    }
                                }
                            }
                            
                            if let element4 = try await base1Iterator.next() {
                                self.stateMachine.withCriticalRegion { stateMachine in
                                    let action = stateMachine.elementProduced((nil, nil, nil, element4))
                                    
                                    switch action {
                                    case .resumeContinuation(let downstreamContinuation, let result):
                                        downstreamContinuation.resume(returning: result)
                                        
                                    case .none:
                                        break
                                    }
                                }
                            } else {
                                let action = self.stateMachine.withCriticalRegion { stateMachine in
                                    stateMachine.upstreamFinished(baseIndex: 3)
                                }
                                
                                switch action {
                                case .resumeContinuationWithNilAndCancelTaskAndUpstreamContinuations(
                                    let downstreamContinuation,
                                    let task,
                                    let upstreamContinuations
                                ):
                                    
                                    upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                                    task.cancel()
                                    
                                    downstreamContinuation.resume(returning: .success(nil))
                                    break loop
                                    
                                case .cancelTaskAndUpstreamContinuations(let task, let upstreamContinuations):
                                    upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                                    task.cancel()
                                    
                                    break loop
                                    
                                case .none:
                                    break loop
                                }
                            }
                        }
                    }
                }
                
                do {
                    try await group.waitForAll()
                } catch {
                    // One of the upstream sequences threw an error
                    self.stateMachine.withCriticalRegion { stateMachine in
                        let action = stateMachine.upstreamThrew(error)
                        
                        switch action {
                        case .cancelTaskAndUpstreamContinuations(let task, let upstreamContinuations):
                            upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                            task.cancel()
                            
                        case .resumeContinuationWithErrorAndCancelTaskAndUpstreamContinuations(
                            let downstreamContinuation,
                            let error,
                            let task,
                            let upstreamContinuations
                        ):
                            upstreamContinuations.forEach { $0.resume(throwing: CancellationError()) }
                            task.cancel()
                            
                            downstreamContinuation.resume(returning: .failure(error))
                            
                        case .none:
                            break
                        }
                    }
                    
                    group.cancelAll()
                }
            }
        }
        
        stateMachine.taskIsStarted(task: task, downstreamContinuation: downStreamContinuation)
    }
}
