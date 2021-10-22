const mongodb = require("mongodb");
const ObjectID = mongodb.ObjectId;
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const express = require("express");
const app = express();
const port = process.env.PORT || 3000;

require('dotenv/config');

// //create express service
app.use(express.json());
app.use(express.urlencoded());

//create mongodb client
const MongoClient = mongodb.MongoClient;
const uri = process.env.DB_CONNECTION;

//Middleware
const jwtVerificationMiddleware = (req, res, next) => {
    console.log("Verification Middleware");

    //verify the token
    let token = req.header("x-jwt-token");
    if(token){
        try {
            let decodedToken = jwt.verify(token, process.env.TOKEN_KEY);
            req.decodedToken = decodedToken;
            next();
            
        } catch (error) {
            return res.status(401).json({error: "Invalid Token", fullError: error});
        }
    }else{
        return res.status(403).json({error:"A token is required for authentication"});
    }
  

};

MongoClient.connect(uri, {useNewUrlParser: true,}, (error, client) => {
    if(error){

        console.log("Unable to connect to the mongoDB.Error", err);

    }else{
        console.log("Connected to the mongodb!!!")

        const db = client.db("user");  //database
        const collection = db.collection("userprofile"); //collection

         //Login Route
         app.post("/login", (req, res, next) => {

            const postData = req.body;

            const email = postData.email;
            const password = postData.password;


            //Check exists email
            collection.find({"email": email}).count((error, number) =>{
                if(error){
                    res.status(404).json({error: error})
                }else{
                    if(number == 0){
                        console.log("Email not exists");
                         res.status(401).json({error: "Email not exists"});
                    }else{
    
                        collection.findOne({"email": email}, (error, user) => {
              
                            if(bcrypt.compareSync(password, user.password)){
                                console.log("Login success");
                                
                                //create JWT token
                                let token = jwt.sign({userId:user._id, email:user.email}, process.env.TOKEN_KEY);
                                res.status(200).json({token: token})
                                
                            }else{
                                console.log("Wrong Password");
                                 res.status(401).json({error: "Wrong Passord"});
                            }
                        })
    
                    }
                }
                
            })

        });

        //Register Route
        app.post("/register", (req, res, next) => {

        
            console.log(req.body);

            const postData = req.body;

            const firstname = postData.firstname;
            const lastname = postData.lastname;
            const age = postData.age;
            const weight = postData.weight;
            const address = postData.address;
            const email = postData.email;
    
          
           
            let userJson = {
                firstname: firstname,
                lastname: lastname,
                age: age,
                weight: weight,
                address: address,
                email: email,
                password: bcrypt.hashSync(postData.password, 10)
        
            }

            //Validate if user exist in our database
            //If not, then insert user data in database
            collection.find

            collection.find({"email":email}).count((error, number) =>{
                if(error){
                    res.status(404).json({error: error})
                }else{

                    if(number!=0){
                        console.log("Email already exists");
                        res.status(409).json({error:"User Already Exist. Please Login"});
                       
                    }else{
    
                        //insert data 
                        collection.insertOne(userJson, (error, response) => {
                            if(error){
                                res.status(404).json({error: error})

                            }else{
                                console.log("Registration success");
                        
                                //create JWT token
                                let token = jwt.sign({userId:response.insertedId, email:email}, process.env.TOKEN_KEY);
                                // return new user
                                res.status(201).json({token: token});
                            }
                          
                            
                        })
    
                    }
                }
            })

        });


        //Protected Profile Route
        app.get("/api/protected/profile", jwtVerificationMiddleware, (req, res, next) => {
            
            const decodedToken = req.decodedToken;
            const email = decodedToken.email;

             //get the user data 
             collection.findOne({email: email}, (error, user) => {
                if(error){
                    res.status(404).json({error: "User not found"});
    
                }else{

                    const userJson = {
                        userId: user._id,
                        firstname: user.firstname,
                        lastname: user.lastname,
                        age: user.age,
                        weight: user.weight,
                        address: user.address,
                        email: user.email,
            
                    }
                    res.status(200).json({user: userJson});
                          
                }
            })
           
        });

        //Update Profile Route
        app.post("/update/profile", (req, res, next) => {

           
            const postData = req.body;

            const firstname = postData.firstname;
            const lastname = postData.lastname;
            const age = postData.age;
            const weight = postData.weight;
            const address = postData.address;
            const email = postData.email;
           
            const userJson = {
                firstname: firstname,
                lastname: lastname,
                age: age,
                weight: weight,
                address: address,
                email: email
            }

              //update data 
              collection.updateOne({ "_id": ObjectID(req.body._id)}, { $set: userJson }, { upsert: true }, (error, response) => {
                  if(error){
                    res.status(404).json({error: error});

                  }else{
                    console.log("Document is updated!!");
                    res.status(200).json({response: response});
                  }
                
            });

        });


    }

});



app.get("/hello", (req,res) => {
    res.status(200).json({message: "Hello"});
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);  
});


