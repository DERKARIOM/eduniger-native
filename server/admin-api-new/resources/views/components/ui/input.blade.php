@props([
    'type' => 'text',
    'name',
    'value' => '',
    'placeholder' => '',
    'required' => false,
    'autofocus' => false,
    'autocomplete' => null,
])

<div class="relative">
    {{-- Icône à gauche --}}
    @if (isset($icon))
        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            {{ $icon }}
        </div>
    @endif

    <input
        type="{{ $type }}"
        name="{{ $name }}"
        id="{{ $name }}"
        value="{{ old($name, $value) }}"
        placeholder="{{ $placeholder }}"
        {{ $required ? 'required' : '' }}
        {{ $autofocus ? 'autofocus' : '' }}
        @if($autocomplete) autocomplete="{{ $autocomplete }}" @endif
        {{ $attributes->merge([
            'class' => 'block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm ' .
                      (isset($icon) ? 'pl-10' : 'pl-3')
        ]) }}
    >
</div>
