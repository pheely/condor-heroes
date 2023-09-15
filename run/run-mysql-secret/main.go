package main

import (
	"net/http"
	"os"

	"github.com/gorilla/mux"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"github.com/yfuruyama/crzerolog"

)

type Employee struct {
	ID        string
	First_Name string
	Last_Name  string
	Department string
	Salary    int
	Age       int
}

type ConnectionParams struct {
	DBUser                 string
	DBPwd                  string
	DBName                 string
	PrivateIP              string
	InstanceConnectionName string
}

var params ConnectionParams
var port string

func init() {
	mustGetenv := func(key string) string {
		value := os.Getenv(key)
		if value == "" {
			log.Fatal().Msg("Fatal error: environment variable not set: " + key)
		}
		return value
	}

	dbUser := mustGetenv("DB_USER")
	dbPassword := mustGetenv("DB_PASS")
	dbName := mustGetenv("DB_NAME")
	privateIp := os.Getenv("DB_PRIVATE_IP")
	instanceConnectionName := mustGetenv("INSTANCE_CONNECTION_NAME")
	port = os.Getenv("PORT")

	if port == "" {
		port = "8080"
	}

	if dbPassword != "changeit" {
		log.Fatal().Msg("wrong password: " + dbPassword)
	}

	params = ConnectionParams {
		DBUser: dbUser,
		DBPwd: dbPassword,
		DBName: dbName,
		PrivateIP: privateIp,
		InstanceConnectionName: instanceConnectionName,
	}
}

func main() {
	rootLogger := zerolog.New(os.Stdout)
	middleware := crzerolog.InjectLogger(&rootLogger)

	r := mux.NewRouter()
	r.Use(middleware)

	s := Service{connection: createDataSource(params)}

	api := r.PathPrefix("/api").Subrouter()
	api.Use(jsonHeader)
	api.Methods(http.MethodOptions).HandlerFunc(
		func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusNoContent)
		})
	api.Path("/employee").Methods(http.MethodPost).HandlerFunc(s.Create)
	api.Path("/employee").Methods(http.MethodGet).HandlerFunc(s.List)
	api.Path("/employee").Methods(http.MethodDelete).HandlerFunc(s.Clear)
	api.Path("/employee/{id}").Methods(http.MethodGet).HandlerFunc(s.Get)
	api.Path("/employee/{id}").Methods(http.MethodDelete).HandlerFunc(s.Delete)
	api.Path("/employee/{id}").Methods(http.MethodPatch, http.MethodPut).HandlerFunc(s.Update)
	api.Path("/help").Methods(http.MethodGet, http.MethodGet).HandlerFunc(s.Help)

	r.PathPrefix("/").Handler(http.FileServer(http.Dir("/app/dist")))
	log.Info().Msg("Listening on port " + port)
	log.Fatal().Err(http.ListenAndServe(":" + port, r)).Msg("Can't start service")
}


