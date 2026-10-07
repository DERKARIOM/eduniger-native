<?php
    function callOpenAI($message){
        header("Access-Control-Allow-Origin: *");
        

        // SECURITE : la cle API OpenAI ne doit JAMAIS etre codee en dur dans le code source
        // (elle etait auparavant exposee ici en clair et doit etre consideree comme compromise :
        // revoquez-la sur platform.openai.com et generez-en une nouvelle).
        // Configurez la variable d'environnement OPENAI_API_KEY sur le serveur (php.ini,
        // configuration Apache/Nginx-FPM, ou fichier .env charge en dehors du depot Git).
        $openai_endpoint = "https://api.openai.com/v1/chat/completions";
        $openai_token = getenv('OPENAI_API_KEY') ?: '';
        if ($openai_token === '') {
            http_response_code(500);
            return json_encode(['error' => "Configuration serveur manquante : OPENAI_API_KEY"]);
        }


        $data = array(
            "model" => "gpt-3.5-turbo",
            "messages" => array(
                array(
                    "role" => "system",
                    "content" => "Vous parlez avec Derkariom"
                ),
                array(
                    "role" => "user",
                    "content" => $message
                )
            ),
            "max_tokens" => 100,
            "temperature" => 0.7
        );
        $headers = array(
            "Content-Type: application/json",
            "Authorization: Bearer ".$openai_token
        );
        $ch = curl_init();
        curl_setopt($ch,CURLOPT_URL,$openai_endpoint);
        curl_setopt($ch,CURLOPT_POST,true);
        curl_setopt($ch,CURLOPT_POSTFIELDS,json_encode($data));
        curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
        curl_setopt($ch,CURLOPT_HTTPHEADER,$headers);

        $response = curl_exec($ch);
        if ($response === false) {
            die(curl_error($ch));
        }
        curl_close($ch);

        return $response;

    }
    if(!empty($_POST['matricule']) AND !empty($_POST['message']))
    {
        try
        {
            $matricule = htmlspecialchars($_POST['matricule']);
            $message = $_POST['message'];
            $reponse = callOpenAI($message);
            $data = json_decode($reponse,true);
            if(isset($data['choices'][0]['message']['content']))
            {
                $filter = $data['choices'][0]['message']['content'];
                $filter = str_ireplace("OpenAi","Ninotech",$filter);
                $filter = str_ireplace("GPT-3","Fabiola-3",$filter);
                $filter = str_ireplace("GPT-3.5-turbo","Fabiola-3.5",$filter);
                $filter = str_ireplace("GPT","Fabiola",$filter);
                $filter = str_ireplace("en Californie","à l'Université Abdou Moumouni de Niamey",$filter);
                $filter = str_ireplace("aux état-unis","au Niger",$filter);
                echo $filter;

            }
        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
        }
    }

?>
