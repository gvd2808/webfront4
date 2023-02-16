package components


import data.AuthState
import data.Credentials
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.html.InputType
import kotlinx.html.blockQuote
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*
import remote.AuthService
import remote.dto.RegisterRequest
import remote.dto.RegisterResponse

external interface AuthFormProps: RProps{
    var login: (Credentials) -> Unit
}


val AuthForm: FunctionalComponent<AuthFormProps> = functionalComponent { props ->
    var loginVar: String = ""
    var passVar: String = ""
    val authService = AuthService.create()
    var response: RegisterResponse = RegisterResponse(register_state = "not requested")

    div("auth_form"){
        div("log_div"){
            label("login_label"){+ "Login"}
            input(classes = "input", type = InputType.text){
                attrs{
                    id = "login_input"
                    onChangeFunction = { event ->
                        loginVar = (event.target as HTMLInputElement).value
                    }
                }
            }
        }
        div("pass_div"){
            label("pass_label"){+ "Password"}
            input(classes = "input", type = InputType.password){
                attrs{
                    id = "password_input"
                    onChangeFunction = { event ->
                        passVar = (event.target as HTMLInputElement).value
                    }
                }
            }
        }

        button {
            + "Login"
            attrs{
                id = "login_button"
                onClickFunction = {
                    props.login(
                        Credentials(
                        login = loginVar,
                        password = passVar,
                        state = AuthState.AUTHORIZED)
                )
                    // TODO : переделать логику входа
                }
            }
        }
        button {
            + "Register"
            attrs{
                id = "register_button"
                onClickFunction = {
                    val request = RegisterRequest(login = loginVar, password_hash = passVar)

                    var job = CoroutineScope(Dispatchers.Main).launch() {
                        val resp: RegisterResponse =
                            try {
                                var res: RegisterResponse? =
                                    authService.registerRequest(request)
                                if (res == null) res = RegisterResponse(register_state = "failed")
                                res
                            } catch (e: IOException) {
                                RegisterResponse(register_state = "failed")
                            }
                        console.log("Request finished: response - $resp")
                        response = resp
                        if (response.register_state == "registered"){
                            console.log("registration success: ${response.register_state}")
                            props.login(Credentials(loginVar, passVar, AuthState.AUTHORIZED))
                        } else{
                            console.error("registration failed: ${response.register_state}" )
                        }
                    }

                    // TODO : переделать логику регистрации
                }
            }
        }
    }
}

fun RBuilder.authForm(loginFunc: (Credentials) -> Unit) = child(AuthForm) {
    attrs.login = loginFunc
}







