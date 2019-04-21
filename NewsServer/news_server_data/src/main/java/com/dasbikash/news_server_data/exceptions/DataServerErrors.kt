/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.news_server_data.exceptions



open class DataServerException : NewsServerException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}
}

class DataServerUnavailable : DataServerException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}
}

class DataNotFound : DataServerException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}
}

class DataPostingException : DataServerException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}
}

class TokenGenerationException : DataServerException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}
}