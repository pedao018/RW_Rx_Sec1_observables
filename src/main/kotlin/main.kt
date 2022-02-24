import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlin.math.roundToInt
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileNotFoundException

fun main(args: Array<String>) {
    practice2()
}

fun practice() {
    exampleOf("just") {
        val observable = Observable.just(listOf(1))
    }
    exampleOf("fromIterable") {
        val observable: Observable<Int> =
            Observable.fromIterable(listOf(1, 2, 3))
    }

    exampleOf("subscribe") {
        val observable = Observable.just(1, 2, 3)
        observable.subscribe { println(it) }
    }

    exampleOf("empty") {
        val observable = Observable.empty<Unit>()
        /*
        You’re using a new subscribeBy method here instead of the subscribe method you used previously.
        subscribeBy is a handy extension method defined in the RxKotlin library, which we’ll touch on later in the book.
        Unlike the subscribe method you used previously, subscribeBy lets you explicitly state what event you want to handle — onNext, onComplete, or onError.
        If you were to only supply the onNext field of subscribeBy, you’d be recreating the subscribe functionality you used above.
        * */
        observable.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") }
        )
    }

    exampleOf("never") {
        val observable = Observable.never<Any>()

        observable.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") }
        )
    }

    exampleOf("range") {
        // 1
        val observable: Observable<Int> = Observable.range(1, 10)

        observable.subscribe {
            // 2
            val n = it.toDouble()
            val fibonacci = ((Math.pow(1.61803, n) -
                    Math.pow(0.61803, n)) / 2.23606).roundToInt()
            println(fibonacci)
        }
    }

    exampleOf("dispose") {
        // 1
        val mostPopular: Observable<String> =
            Observable.just("A", "B", "C")
        // 2
        val subscription = mostPopular.subscribe {
            // 3
            println(it)
        }
        subscription.dispose()
    }


    //Nói chung mục đích của code mẫu CompositeDisposable này là tránh leak memory
    /*
    This is the pattern you’ll use most frequently: creating and subscribing to an observable and immediately adding the subscription to a CompositeDisposable.
    Why bother with disposables at all? If you forget to call dispose() on a Disposable when you’re done with the subscription,
    or in some other way cause the observable to terminate at some point, you will probably leak memory.
    * */
    exampleOf("CompositeDisposable") {
        val subscriptions = CompositeDisposable()
        val disposable = Observable.just("A", "B", "C")
            .subscribe {
                println(it)
            }
        subscriptions.add(disposable)
        subscriptions.dispose()
    }


    /* Nói chung là, cuối dòng code của .create phải có emitter.onComplete() để stop tránh memory leak
    Run those changes. Congratulations, you’ve just leaked memory! :] The observable will never finish, and since you never disposed of the Disposable returned by Observable.create the sequence will never be canceled.
    * */
    exampleOf("create") {
        val disposables = CompositeDisposable()

        Observable.create<String> { emitter ->
            // 1
            emitter.onNext("1")

            //emitter.onError(RuntimeException("Error"))

            // 2
            //emitter.onComplete() -> Nó bị comment nên dẫn đến memory leak, mở comment ra để tránh memory leak

            // 3
            emitter.onNext("?")
        }.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") },
            onError = { println("Error") }
        )

    }

    exampleOf("defer") {
        val disposables = CompositeDisposable()
        // 1
        var flip = false
        // 2
        val factory: Observable<Int> = Observable.defer {
            // 3
            flip = !flip
            // 4
            if (flip) {
                Observable.just(1, 2, 3)
            } else {
                Observable.just(4, 5, 6)
            }
        }
        for (i in 0..3) {
            disposables.add(
                factory.subscribe {
                    println(it)
                }
            )
        }
        disposables.dispose()
    }

    exampleOf("Single") {
        val subscriptions = CompositeDisposable()
        fun loadText(filename: String): Single<String> {
            return Single.create create@{ emitter ->
                val file = File(filename)
                if (!file.exists()) {
                    emitter.onError(FileNotFoundException("Can’t find $filename"))
                    return@create
                }
                val contents = file.readText(Charsets.UTF_8)
                emitter.onSuccess(contents)

            }
        }

        val observer = loadText("Copyright.txt")
            .subscribeBy(
                onSuccess = { println(it) },
                onError = { println("Error, $it") }
            )
        subscriptions.add(observer)
    }

    exampleOf("never chanllenge") {
        val subscriptions = CompositeDisposable()
        val observable = Observable.never<Any>()
        val subscription = observable
            .doOnNext { println(it) }
            .doOnComplete { println("Completed") }
            .doOnSubscribe { println("Subscribed") }
            .doOnDispose { println("Disposed") }
            .subscribeBy(onNext = {
                println(it)
            }, onComplete = {
                println("Completed")
            })
        subscriptions.add(subscription)
        subscriptions.dispose()
    }
}

fun practice2() {
    exampleOf("just") {
        val observable = Observable.just(listOf(1))
        val observable2 = Observable.just(1, 2, 3)
    }

    exampleOf("fromIterable") {
        val observable = Observable.fromIterable(listOf(1, 2, 3))
    }

    exampleOf("subscribe") {
        val observable = Observable.just(1, 2, 3)
        val observable2 = Observable.fromIterable(listOf(1, 2, 3))
        observable.subscribe { println(it) }
        observable2.subscribe { print(it) }
    }

    exampleOf("empty") {
        val observable = Observable.empty<Unit>()

        observable.subscribeBy(
            onNext = { print(it) },
            onComplete = { println("Complete") }
        )
    }

    exampleOf("never") {
        val observable = Observable.never<Any>()
        observable.subscribeBy(
            onNext = { print(it) },
            onComplete = { println("Complete") }
        )
    }

    exampleOf("range") {
        val observable = Observable.range(1, 10)
        observable.subscribe {
            println("$it")
        }
    }

    exampleOf("dispose") {
        val mostPopular: Observable<String> = Observable.just("A", "B", "C")
        val subscription = mostPopular.subscribe { println(it) }
        subscription.dispose()
    }

    exampleOf("CompositeDisposeable") {
        val subscriptions = CompositeDisposable()
        val observable = Observable.just("A", "B", "C")
        val observable2 = Observable.just(1, 2, 3)
        val sub1 = observable.subscribe { println(it) }
        val sub2 = observable2.subscribe { println(it) }
        subscriptions.add(sub1)
        subscriptions.add(sub2)
        subscriptions.dispose()
    }

    exampleOf("create") {
        val disposable = CompositeDisposable()
        val observable = Observable.create<String> { emitter ->
            emitter.onNext("1")
            emitter.onNext("2")
            emitter.onComplete()
            emitter.onNext("?")
        }
        val subscribe = observable.subscribeBy(onNext = { println(it) },
            onComplete = { println("On Complete") },
            onError = { println("On Error") })
    }

    exampleOf("defer") {
        val compositeDisposable = CompositeDisposable()
        var flip = false
        val observable = Observable.defer {
            flip = !flip
            if (flip)
                Observable.just(1, 2, 3)
            else
                Observable.just(4, 5, 6)
        }

        for (i in 0..3) {
            compositeDisposable.add(
                observable.subscribe { print(it) }
            )
            println()
        }
        compositeDisposable.dispose()
    }

    exampleOf("Single") {
        val compositeDisposable = CompositeDisposable()
        fun loadText(fileName: String): Single<String> {
            return Single.create create@{ emitter ->
                val file = File(fileName)
                if (!file.exists()) {
                    emitter.onError(FileNotFoundException("Can't find $fileName"))
                    return@create
                }
                val contents = file.readText(Charsets.UTF_8)
                emitter.onSuccess(contents)
            }
        }

        val subscribe = loadText("Copyright.txt").subscribeBy(
            onSuccess = { print(it) },
            onError = { println("Error, $it") }
        )
        compositeDisposable.add(subscribe)
    }

    exampleOf("Never Challenge") {
        val compositeDisposable = CompositeDisposable()
        val observable = Observable.never<Any>()
        compositeDisposable.add(
            observable
                .doOnSubscribe {
                    println("Never Subscribe")
                }
                .subscribeBy(
                    onNext = { print(it) },
                    onComplete = { println("Complete") }
                )
        )
        compositeDisposable.dispose()
    }

    exampleOf("never chanllenge") {
        val subscriptions = CompositeDisposable()
        val observable = Observable.never<Any>()
        val subscription = observable
            .doOnNext { println(it) }
            .doOnComplete { println("Completed") }
            .doOnSubscribe { println("Subscribed") }
            .doOnDispose { println("Disposed") }
            .subscribeBy(onNext = {
                println(it)
            }, onComplete = {
                println("Completed")
            })
        subscriptions.add(subscription)
        subscriptions.dispose()
    }
}