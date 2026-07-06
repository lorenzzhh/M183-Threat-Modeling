import React, {useState} from 'react';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import './App.css';
import './css/mvp.css';
import Home from './pages/Home';
import Layout from "./pages/Layout";
import NoPage from "./pages/NoPage";
import LoginUser from "./pages/user/LoginUser";
import RegisterUser from "./pages/user/RegisterUser";
import Secrets from "./pages/secret/Secrets";
import NewCredential from "./pages/secret/NewCredential";
import NewCreditCard from "./pages/secret/NewCreditCard";
import NewNote from "./pages/secret/NewNote";

/**
 * App
 * @author Peter Rutschmann
 */
function App() {
    // token/userId are filled in after a successful login (see LoginUser.js)
    // and sent along with every secret request so the backend knows who is
    // really asking - instead of trusting whatever email a request claims.
    const [loginValues, setLoginValues] = useState({
        email: "",
        password: "",
        token: "",
        userId: null,
    });
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Layout loginValues={loginValues}/>}>
                    <Route index element={<Home/>}/>
                    <Route path="/user/login" element={<LoginUser loginValues={loginValues} setLoginValues={setLoginValues}/>}/>
                    <Route path="/user/register" element={<RegisterUser loginValues={loginValues} setLoginValues={setLoginValues}/>}/>
                    <Route path="/secret/secrets" element={<Secrets loginValues={loginValues}/>}/>
                    <Route path="/secret/newcredential" element={<NewCredential loginValues={loginValues}/>}/>
                    <Route path="/secret/newcreditcard" element={<NewCreditCard loginValues={loginValues}/>}/>
                    <Route path="/secret/newnote" element={<NewNote loginValues={loginValues}/>}/>
                    <Route path="*" element={<NoPage/>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    )
}

export default App;
