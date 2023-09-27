package main

import (
	"net/http"
	"os"

	"github.com/gorilla/mux"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"github.com/yfuruyama/crzerolog"

	"github.com/pheely/employee-api/internal/handler"
	"github.com/pheely/employee-api/internal/stores"
)

var port string
var connectionParameters stores.ConnectionParameters

func init() {
	mustGetEnv := func(key string) string {
		value := os.Getenv(key)
		if value == "" {
			log.Fatal().Msg("Fatal error: environment variable not set: " + key)
		}
		return value
	}

	connectionParameters = stores.ConnectionParameters{
		DBUser:                 mustGetEnv("DB_USER"),
		DBPwd:                  mustGetEnv("DB_PASS"),
		DBName:                 mustGetEnv("DB_NAME"),
		PrivateIP:              os.Getenv("DB_PRIVATE_IP"),
		InstanceConnectionName: mustGetEnv("INSTANCE_CONNECTION_NAME"),
	}

	port = os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}
}

func main() {
	s := handler.Service{
		Connection: stores.NewDataSource(connectionParameters),
	}

	r := CreateRouter(s)
	r.PathPrefix("/").Handler(http.FileServer(http.Dir("/app/dist")))
	log.Info().Msg("Listening on port " + port)
	log.Fatal().Err(http.ListenAndServe(":"+port, r)).Msg("Can't start service")
}

func CreateRouter(h handler.Service) *mux.Router {
	rootLogger := zerolog.New(os.Stdout)
	middleware := crzerolog.InjectLogger(&rootLogger)
	r := mux.NewRouter()
	r.Use(middleware)

	api := r.PathPrefix("/api").Subrouter()
	api.Use(handler.JsonHeader)
	api.Methods(http.MethodOptions).HandlerFunc(
		func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusNoContent)
		})
	api.Path("/employee").Methods(http.MethodPost).HandlerFunc(h.CreateEmployee)
	api.Path("/employee").Methods(http.MethodGet).HandlerFunc(h.GetAllEmployees)
	api.Path("/employee").Methods(http.MethodDelete).HandlerFunc(h.DeleteAllEmployees)
	api.Path("/employee/{id}").Methods(http.MethodGet).HandlerFunc(h.GetEmployee)
	api.Path("/employee/{id}").Methods(http.MethodDelete).HandlerFunc(h.DeleteEmployee)
	api.Path("/employee/{id}").Methods(http.MethodPatch, http.MethodPut).HandlerFunc(h.UpdateEmployee)
	api.Path("/help").Methods(http.MethodGet, http.MethodGet).HandlerFunc(h.Help)

	return r
}
