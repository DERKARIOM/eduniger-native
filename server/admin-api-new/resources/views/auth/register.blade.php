@extends('layouts.app')

@section('content')
<div class="min-h-screen flex items-center justify-center bg-[#00A06A] px-4 py-12">
    <div class="w-full max-w-md bg-white shadow-lg rounded-2xl p-8 space-y-6">
        <div class="text-center">
            <img src="{{ asset('images/edu niger test.svg') }}" alt="Logo" class="mx-auto w-20 h-20">
            <h2 class="mt-4 text-2xl font-bold text-gray-800">Créer un compte</h2>
            <p class="text-sm text-gray-500">Veuillez remplir les champs ci-dessous</p>
        </div>

        <form method="POST" action="{{ route('register') }}" class="space-y-4">
            @csrf

            <div>
                <x-ui-label for="name">Nom complet</x-ui-label>
                <x-ui-input name="name" type="text" placeholder="ex: Mahamadou Ali" required autofocus autocomplete="name">
                    <x-slot name="icon">
                        <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" stroke-width="1.5"
                             viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                  d="M5.121 17.804A6.5 6.5 0 0112 13.5a6.5 6.5 0 016.879 4.304M12 11.5a4 4 0 100-8 4 4 0 000 8z" />
                        </svg>
                    </x-slot>
                </x-ui-input>
                <x-ui-error field="name" />
            </div>

            <div>
                <x-ui-label for="email">Email</x-ui-label>
                <x-ui-input name="email" type="email" placeholder="ex: nom@domaine.ne" required autocomplete="email">
                    <x-slot name="icon">
                        <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" stroke-width="1.5"
                             viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                  d="M4.5 6.75h15v10.5h-15V6.75z" />
                        </svg>
                    </x-slot>
                </x-ui-input>
                <x-ui-error field="email" />
            </div>

            <div>
                <x-ui-label for="password">Mot de passe</x-ui-label>
                <x-ui-password-input name="password" placeholder="********" required autocomplete="new-password" />
                <x-ui-error field="password" />
            </div>

            <div>
                <x-ui-label for="password_confirmation">Confirmer le mot de passe</x-ui-label>
                <x-ui-password-input name="password_confirmation" placeholder="********" required autocomplete="new-password" />
                <x-ui-error field="password_confirmation" />
            </div>

            <div>
                <button type="submit"
                        class="w-full py-2 px-4 bg-[#00A06A] hover:bg-[#008856] text-white font-semibold rounded-md shadow-sm transition duration-150">
                    Créer un compte
                </button>
            </div>
        </form>

        <div class="text-center text-sm text-gray-600">
            Vous avez déjà un compte ?
            <a href="{{ route('login') }}" class="text-[#00A06A] hover:underline font-semibold">
                Se connecter
            </a>
        </div>
    </div>
</div>
@endsection
