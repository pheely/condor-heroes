package handler

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/pheely/employee-api/internal/stores"
)

func JsonHeader(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Add("content-type", "application/json")
		next.ServeHTTP(w, r)
	})
}

type Service struct {
	Connection stores.DataSource
	RedisStore stores.RedisStore
}

type employee struct {
	Id	   		string `json:"id"`
	First_Name 	string `json:"first_name"`
	Last_Name  	string `json:"last_name"`
	Department 	string `json:"department"`
	Salary    	int    `json:"salary"`
	Age      	int    `json:"age"`
}

func convertToEmployeeJson(t stores.Employee) employee {
	jsonT := employee{
		Id:				t.ID,
		First_Name:     t.First_Name,
		Last_Name: 		t.Last_Name,
		Department:     t.Department,
		Salary:       	t.Salary,
		Age: 			t.Age,
	}
	return jsonT
}

func (s *Service) Help(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusOK)
	counter, err := s.RedisStore.Increment()
	if err != nil {
		http.Error(w, "Error incrementing visitor counter", http.StatusInternalServerError)
		return
	}
	fmt.Fprintf(w, "Employee API v1. You are vistor number %d\n", counter)
}

func (s *Service) GetAllEmployees(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusOK)
	list, err := s.Connection.GetAll()
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	result := []employee{}
	for _, t := range list {
		result = append(result, convertToEmployeeJson(t))
	}
	json.NewEncoder(w).Encode(result)
}

func (s *Service) DeleteAllEmployees(w http.ResponseWriter, r *http.Request) {
	s.Connection.DeleteAll()
	s.GetAllEmployees(w, r)
}

func (s *Service) DeleteEmployee(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id := vars["id"]
	err := s.Connection.Delete(id)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}

func (s *Service) UpdateEmployee(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id := vars["id"]

	decoder := json.NewDecoder(r.Body)

	var newT stores.Employee
	err := decoder.Decode(&newT)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	res, err := s.Connection.Update(id, &newT)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	if res == nil {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	json.NewEncoder(w).Encode(convertToEmployeeJson(*res))
}

func (s *Service) GetEmployee(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	t, err := s.Connection.Get(vars["id"])
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	if t != nil {
		json.NewEncoder(w).Encode(convertToEmployeeJson(*t))
		return
	}
	w.WriteHeader(http.StatusNotFound)
}

func (s *Service) CreateEmployee(w http.ResponseWriter, r *http.Request) {
	decoder := json.NewDecoder(r.Body)
	var t stores.Employee
	err := decoder.Decode(&t)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	err = s.Connection.Create(&t)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	json.NewEncoder(w).Encode(convertToEmployeeJson(t))
}
